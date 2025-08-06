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

//TODO: Singleton Pattern umsetzen
//TODO: Unique ID für IOManager erstellen
//TODO: Verwaltung von mehreren Chatfenstern (z.B. für mehrere Verbindungen) implementieren

public class IOManager implements IOAccess {
    private final Socket socket; // Socket für die Kommunikation
    private final CryptoManager cryptoManager; // Manager für die Verschlüsselung
    private final ChatWindow chatWindow; // Referenz auf das Chatfenster
    private final StartWindowAccess startWindowAccess;
    private final BlockingQueue<ChatMessage> chatQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<SystemMessage> systemQueue = new LinkedBlockingQueue<>();
    private final Gson gson = new Gson(); // Gson-Objekt zur Umwandlung von Objekten in JSON

    // Konstruktor, der die Instanzen initialisiert
    public IOManager(Socket socket, String passkey, Boolean isHost, StartWindowAccess startWindowAccess) {
        this.socket = socket;
        this.cryptoManager = new CryptoManager(passkey);
        this.startWindowAccess = startWindowAccess;
        this.chatWindow = new ChatWindow(isHost, this, startWindowAccess);
    }

    public void startCommunicationThreads() {
        startReceiver();          // Nachrichten vom Socket empfangen
        startChatHandler();       // Chat-Nachrichten verarbeiten
        startSystemHandler();    // Steuerbefehle verarbeiten
    }

    // Methode zum Empfangen von Nachrichten/Steuerbefehlen
    private void startReceiver() {
        Thread receiverThread = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                // Liest Zeile für Zeile vom InputStream des Sockets
                while ((line = in.readLine()) != null) {
                    try {
                        // Entschlüsselt die empfangene Nachricht
                        String decrypted = cryptoManager.decrypt(line);
                        // Wandelt den entschlüsselten JSON-String in ein JsonObject um, um den Typ abzufragen
                        JsonObject jsonObject = JsonParser.parseString(decrypted).getAsJsonObject();
                        // Überprüft den Wert des "type"-Feldes im JSON
                        String type = jsonObject.get("type").getAsString();
                        // Prüft den Typ der Nachricht
                        if (type.equals("SYSTEM")) {
                            // Wandelt die entschlüsselte Nachricht von JSON in ein SystemMessage-Objekt um
                            SystemMessage sysMSG = gson.fromJson(decrypted, SystemMessage.class);
                            // Fügt die Nachricht zur systemQueue hinzu
                            systemQueue.put(sysMSG);                            
                        } else if (type.equals("CHAT")) {
                            ChatMessage msg = gson.fromJson(decrypted, ChatMessage.class);
                            // Fügt die Nachricht zur chatQueue hinzu
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

    // Dieser Thread verarbeitet die Chat-Nachrichten, die in der chatQueue gespeichert sind
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

    // Dieser Thread verarbeitet die System-Nachrichten, die in der systemQueue gespeichert sind
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
                                Thread.sleep(5000); // 5 Sekunden warten
                                chatWindow.dispose(); // Schließt das Chatfenster
                                startWindowAccess.onRemoteDisconnect(); // Benachrichtigt den Listener über die Trennung
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

    // Methode zum Senden einer Nachricht
    public void sendChatMessage() {
        try {
            // Holt den Text aus dem Eingabefeld und entfernt Leerzeichen
            String text = chatWindow.getInputText().trim();
            if (text.isEmpty()) return; // Wenn der Text leer ist, wird die Methode beendet

            // Holt die lokale IP-Adresse des Geräts
            String localIp = InetAddress.getLocalHost().getHostAddress();
            // Erstellt eine Nachricht mit dem Text und der Sender-IP
            ChatMessage message = new ChatMessage(text, localIp);

            // Zeigt die Nachricht im Chatbereich an
            chatWindow.appendMessage("[" + message.getTimestamp() + "] " + message.getSenderIp() + ": " + message.getText(), Color.GRAY);

            // Wandelt die Nachricht in JSON um und verschlüsselt sie
            String json = gson.toJson(message);
            String encrypted = cryptoManager.encrypt(json);

            // Sendet die verschlüsselte Nachricht über den Socket
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(encrypted);

            // Setzt das Eingabefeld zurück
            chatWindow.clearInput();
        } catch (Exception e) {
            // Falls ein Fehler auftritt, wird eine Fehlermeldung im Chatbereich angezeigt
            chatWindow.appendMessage("[Unerwarteter Fehler beim Senden] " + e.getMessage(), Color.ORANGE);
        }
    }

    // Methode zum Senden einer System-Nachricht (z. B. zur Trennung der Verbindung)
    @Override
    public void sendSystemMessage(String subtype, String payload) {
        try {
            // Erstellt eine Systemnachricht mit dem Payload "DISCONNECT"
            SystemMessage message = new SystemMessage(subtype, payload, InetAddress.getLocalHost().getHostAddress());
            // Wandelt die Nachricht in JSON um und verschlüsselt sie
            String json = gson.toJson(message);
            String encrypted = cryptoManager.encrypt(json);

            // Sendet die verschlüsselte Nachricht über den Socket
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(encrypted);
        } catch (Exception e) {
            startWindowAccess.onStatusUpdate("[Fehler beim Senden von Systemnachricht - DISCONNECT] " + e.getMessage());
        }
    }

    // Methode zum schließen des Fensters (Kann über IOAccess aufgerufen werden)
    @Override
    public void closeChatWindow() {
        if (this.chatWindow != null) {
            this.chatWindow.dispose();
        }
    }
}