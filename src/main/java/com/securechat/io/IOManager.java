/**
 * Verantwortlich für die Ein- und Ausgabe (IO) im SecureChat-System.
 * <p>
 * Die Klasse {@code IOManager} verwaltet die verschlüsselte Kommunikation über einen Socket,
 * verarbeitet eingehende Chat- und Systemnachrichten und steuert die Anzeige im zugehörigen Chatfenster.
 * Sie setzt auf Threads für Empfang und Verarbeitung, nutzt intern Warteschlangen und übernimmt
 * die Verschlüsselung/Entschlüsselung über den {@link CryptoManager}.
 * 
 * @author Milos Hornik
 */
package com.securechat.io;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.securechat.gui.ChatWindow;
import com.securechat.gui.StartWindowAccess;
import com.securechat.model.ChatMessage;
import com.securechat.model.SystemMessage;
import com.securechat.security.CryptoManager;

import java.io.*;
import java.awt.Color;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IOManager implements IOAccess {
    private final Socket socket;
    private final CryptoManager cryptoManager;
    private final ChatWindow chatWindow;
    private final StartWindowAccess startWindowAccess;
    private final BlockingQueue<ChatMessage> chatQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<SystemMessage> systemQueue = new LinkedBlockingQueue<>();
    private final Gson gson = new Gson();

    /**
     * Konstruktor, der die Instanzen initialisiert.
     * 
     * @param socket           Verwendeter Socket
     * @param passkey          Schlüssel für Verschlüsselung
     * @param isHost           {@code true} wenn Host, {@code false} wenn Client
     * @param startWindowAccess Zugriff auf das Startfenster für Callbacks
     */
    public IOManager(Socket socket, String passkey, Boolean isHost, StartWindowAccess startWindowAccess) {
        this.socket = socket;
        this.cryptoManager = new CryptoManager(passkey);
        this.startWindowAccess = startWindowAccess;
        this.chatWindow = new ChatWindow(isHost, this, startWindowAccess);
    }

    /**
     * Startet die Threads für Empfang und Verarbeitung von Nachrichten.
     */
    public void startCommunicationThreads() {
        startReceiver();
        startChatHandler();
        startSystemHandler();
    }

    /**
     * Thread, der verschlüsselte Nachrichten vom Socket liest und in die passenden Queues stellt.
     */
    private void startReceiver() {
        Thread receiverThread = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        String decrypted = cryptoManager.decrypt(line);
                        JsonObject jsonObject = JsonParser.parseString(decrypted).getAsJsonObject();
                        String type = jsonObject.get("type").getAsString();
                        if (type.equals("SYSTEM")) {
                            SystemMessage sysMSG = gson.fromJson(decrypted, SystemMessage.class);
                            systemQueue.put(sysMSG);                            
                        } else if (type.equals("CHAT")) {
                            ChatMessage msg = gson.fromJson(decrypted, ChatMessage.class);
                            chatQueue.put(msg);
                        } else {
                            chatWindow.appendMessage("[Unbekannter Nachrichtentyp] " + type, Color.ORANGE);
                        }
                    } catch (Exception ex) {
                        chatWindow.appendMessage("[Fehler beim Entschlüsseln] " + ex.getMessage(), Color.ORANGE);
                    }
                }
            } catch (IOException e) {
                chatWindow.appendMessage("[Verbindung unerwartet getrennt] " + e.getMessage(), Color.RED);
            }
        }, "Receiver-Thread");
        receiverThread.start();
    }

    /**
     * Thread zur Verarbeitung und Anzeige von Chat-Nachrichten.
     */
    private void startChatHandler() {
        Thread chatHandlerThread = new Thread(() -> {
            while (true) {
                try {
                    ChatMessage msg = chatQueue.take();
                    chatWindow.appendMessage("[" + msg.getTimestamp() + "] " + msg.getSenderIp() + ": " + msg.getText(), Color.BLUE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "ChatHandler-Thread");
        chatHandlerThread.start();
    }

    /**
     * Thread zur Verarbeitung und Anzeige von System-Nachrichten.
     */
    private void startSystemHandler() {
        Thread systemHandlerThread = new Thread(() -> {
            while (true) {
                try {
                    SystemMessage sysMSG = systemQueue.take();
                    String subtype = sysMSG.getSubtype();
                    String payload = sysMSG.getPayload();
                    String senderIP = sysMSG.getSenderIp();
                    
                    if (subtype.equals("REMOTESTATE")) {
                        if (payload.equals("DISCONNECT")) {
                            chatWindow.appendMessage("[SYSTEM] " + senderIP + " hat die Verbindung getrennt", Color.RED);
                            chatWindow.appendMessage("Das Fenster wird in 5 Sekunden geschlossen.", Color.RED);
                            try {
                                Thread.sleep(5000);
                                chatWindow.dispose();
                                startWindowAccess.onRemoteDisconnect();
                                break;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }   
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "SystemHandler-Thread");
        systemHandlerThread.start();
    }

    /**
     * Sendet eine Chat-Nachricht an den Kommunikationspartner.
     */
    public void sendChatMessage() {
        try {
            String text = chatWindow.getInputText().trim();
            if (text.isEmpty()) return;
            String localIp = InetAddress.getLocalHost().getHostAddress();
            ChatMessage message = new ChatMessage(text, localIp);
            chatWindow.appendMessage("[" + message.getTimestamp() + "] " + message.getSenderIp() + ": " + message.getText(), Color.GRAY);
            String json = gson.toJson(message);
            String encrypted = cryptoManager.encrypt(json);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(encrypted);
            chatWindow.clearInput();
        } catch (Exception e) {
            chatWindow.appendMessage("[Unerwarteter Fehler beim Senden] " + e.getMessage(), Color.ORANGE);
        }
    }

    /**
     * Sendet eine System-Nachricht (z. B. für Trennungsereignisse).
     * 
     * @param subtype  Subtyp der System-Nachricht (z. B. "REMOTESTATE")
     * @param payload  Nutzdaten der System-Nachricht
     */
    @Override
    public void sendSystemMessage(String subtype, String payload) {
        try {
            SystemMessage message = new SystemMessage(subtype, payload, InetAddress.getLocalHost().getHostAddress());
            String json = gson.toJson(message);
            String encrypted = cryptoManager.encrypt(json);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(encrypted);
        } catch (Exception e) {
            startWindowAccess.onStatusUpdate("[Fehler beim Senden von Systemnachricht - DISCONNECT] " + e.getMessage());
        }
    }

    /**
     * Schließt das Chatfenster. Kann über das {@link IOAccess}-Interface aufgerufen werden.
     */
    @Override
    public void closeChatWindow() {
        if (this.chatWindow != null) {
            this.chatWindow.dispose();
        }
    }
}