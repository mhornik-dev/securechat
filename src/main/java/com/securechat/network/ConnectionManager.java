/**
 * Verantwortlich für den Aufbau, die Überwachung und das Schließen der Netzwerkverbindung im SecureChat-System.
 * <p>
 * Die Klasse {@code ConnectionManager} verwaltet die Host- und Client-Logik für den Verbindungsaufbau,
 * übernimmt die Validierung der Eingaben, bietet Rückrufe für den GUI-Status und startet den {@link IOManager}
 * für die eigentliche Kommunikationslogik.
 * 
 * @author Milos Hornik
 */
package com.securechat.network;

import java.io.*;
import java.net.*;

import com.securechat.gui.StartWindowAccess;
import com.securechat.io.IOAccessReceiver;
import com.securechat.io.IOManager;
import com.securechat.security.PasskeyManager;

public class ConnectionManager {
    private final Boolean isHost;
    private final String passkey;
    private final StartWindowAccess startWindowAccess;
    private final int PORT = 5000;
    private final IOAccessReceiver receiver;
    private String remoteIp;
    private Socket socket;
    private ServerSocket serverSocket;
    private IOManager ioManager;

    /**
     * Konstruktor, der die Instanzen initialisiert.
     * 
     * @param isHost           {@code true} für Host, {@code false} für Client
     * @param remoteIp         IP-Adresse des Partners
     * @param passkey          Gemeinsamer Passkey
     * @param startWindowAccess Zugriff auf GUI-Komponente für Statusmeldungen
     * @param receiver         Empfänger für IOAccess-Objekt (z. B. StartWindow)
     */
    public ConnectionManager(Boolean isHost, String remoteIp, String passkey, StartWindowAccess startWindowAccess, IOAccessReceiver receiver) {
        this.isHost = isHost;
        this.remoteIp = remoteIp;
        this.passkey = passkey;
        this.startWindowAccess = startWindowAccess;
        this.receiver = receiver;
    }

    /**
     * Validiert die Eingaben vor dem Verbindungsaufbau.
     * 
     * @param isHost           Host-Flag
     * @param isClient         Client-Flag
     * @param ip               Ziel-IP
     * @param passkey          Passkey
     * @param startWindowAccess GUI-Callback
     * @return {@code true} wenn Eingaben gültig, sonst {@code false}
     */
    public static Boolean prepareConnection(Boolean isHost, Boolean isClient, String ip, String passkey, StartWindowAccess startWindowAccess) {
        if (isHost == false && isClient == false) {
            startWindowAccess.onStatusUpdate("Bitte Host oder Client auswählen.");
            return false;
        }       
        if (!isHost) {
            if (ip == null || ip.isEmpty()) {
                startWindowAccess.onStatusUpdate("Bitte IP-Adresse eingeben.");
                return false;
            }
            if (!isValidIpAddress(ip)) {
                startWindowAccess.onStatusUpdate("Bitte eine gültige IP-Adresse eingeben.");
                return false;
            }
        }
        if (passkey == null || passkey.isEmpty()) {
            startWindowAccess.onStatusUpdate("Bitte Passkey eingeben.");
            return false;
        }
        if (passkey.length() < 8) {
            startWindowAccess.onStatusUpdate("Passkey muss mindestens 8 Zeichen lang sein.");
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob eine Zeichenkette eine gültige IPv4-Adresse ist.
     * 
     * @param ip IP-Adresse als String
     * @return {@code true} wenn gültig, sonst {@code false}
     */
    private static Boolean isValidIpAddress(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Startet den Verbindungsaufbau in einem neuen Thread.
     */
    public void startConnection() {
        if (socket != null) {
            startWindowAccess.onStatusUpdate("Verbindung bereits aktiv.");
            return;
        }
        new Thread(() -> {
            try {
                if (isHost) {
                    startHost(); // Starte als Host
                    receiver.setIOAccess(ioManager);
                } else {
                    startClient(); // Starte als Client
                    receiver.setIOAccess(ioManager);
                }
            } catch (Exception ignored) { 
            }
        }, "ConnectionManager-Thread").start();
    }

    /**
     * Schliesst die bestehende Verbindung oder den ServerSocket.
     */
    public void closeConnection() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
                startWindowAccess.onStatusUpdate("Host wurde beendet");
                ConnectionState.setState(ConnectionState.DISCONNECTED);
            } else if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
                ConnectionState.setState(ConnectionState.DISCONNECTED);
            } else {
                startWindowAccess.onStatusUpdate("Verbindungsversuch abgebrochen");
                ConnectionState.setState(ConnectionState.ABORTED);
            }
        } catch (IOException e) {
            startWindowAccess.onStatusUpdate("Fehler beim Schließen der Verbindung: " + e.getMessage());
        }
    }

    /**
     * Startet einen ServerSocket und wartet auf eingehende Verbindungen.
     * 
     * @throws Exception bei Fehlern während des Verbindungsaufbaus
     */
    private void startHost() throws Exception {
        startWindowAccess.onStatusUpdate("Starte Host...");
        serverSocket = new ServerSocket(PORT);
        startWindowAccess.onStatusUpdate("Host gestartet");
        while(true) {
            ConnectionState.setState(ConnectionState.WAITING);
            startWindowAccess.onConnecting();
            startWindowAccess.onStatusUpdate("Warte auf eingehende Verbindung...");
            socket = serverSocket.accept();
            ConnectionState.setState(ConnectionState.CONNECTING);
            startWindowAccess.onStatusUpdate("Anfrage von " + socket.getInetAddress().getHostAddress());
            startWindowAccess.onStatusUpdate("Empfange Passkey...");

            if (PasskeyManager.verifyPasskey(socket, passkey, isHost)) {
                startWindowAccess.onStatusUpdate("Passkey gültig");
                startIOManager();
                ConnectionState.setState(ConnectionState.CONNECTED);
                startWindowAccess.onConnected();
                break;
            } else {
                ConnectionState.setState(ConnectionState.FAILED);
                startWindowAccess.onConnectionFailed("Ungültiger Passkey. Verbindung abgelehnt");
                socket.close();
            }
        }
    }

    /**
     * Baut eine Verbindung als Client zu einem Host auf.
     * 
     * @throws Exception bei Fehlern während des Verbindungsaufbaus
     */
    private void startClient() throws Exception {
        try {
            ConnectionState.setState(ConnectionState.CONNECTING);
            startWindowAccess.onConnecting();
            startWindowAccess.onStatusUpdate("Versuche Verbindung zu " + remoteIp + "...");
            Socket newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(remoteIp, PORT), 10000);
            socket = newSocket;

            startWindowAccess.onStatusUpdate("Verbindung erfolgreich");
            startWindowAccess.onStatusUpdate("Sende Passkey...");

            if (PasskeyManager.verifyPasskey(socket, passkey, isHost)) {
                startWindowAccess.onStatusUpdate("Passkey bestätigt");
                startIOManager();
                ConnectionState.setState(ConnectionState.CONNECTED);
                startWindowAccess.onConnected();
            } else {
                ConnectionState.setState(ConnectionState.FAILED);
                startWindowAccess.onConnectionFailed("Ungültiger Passkey. Verbindung fehlgeschlagen.");
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            if (ConnectionState.getState() == ConnectionState.ABORTED) {
                return;
            }
            startWindowAccess.onConnectionFailed("Verbindungsversuch fehlgeschlagen: Host ist nicht erreichbar!");
            ConnectionState.setState(ConnectionState.FAILED);
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        }
    }

    /**
     * Startet den {@link IOManager} für die Kommunikation.
     */
    private void startIOManager() {
        try {
            String passkey = this.passkey;
            Boolean isHost = this.isHost;
            startWindowAccess.onStatusUpdate("Starte Chat...");
            ioManager = new IOManager(socket, passkey, isHost, startWindowAccess);
            ioManager.startCommunicationThreads();
            startWindowAccess.onStatusUpdate("Chat gestartet");
        } catch (Exception e) {
            startWindowAccess.onConnectionFailed("Fehler beim Starten des IOManagers: " + e.getMessage());
        }
    }
}