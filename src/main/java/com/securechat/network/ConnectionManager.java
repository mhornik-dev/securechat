package com.securechat.network;

import java.io.*;
import java.net.*;

import com.securechat.gui.StartWindowAccess;
import com.securechat.io.IOAccessReceiver;
import com.securechat.io.IOManager;
import com.securechat.security.PasskeyManager;

//TODO: CLIENT/HOST in eigenes Modell auslagern

public class ConnectionManager {
    private final Boolean isHost; // Gibt an, ob die Instanz als Host agiert
    private final String passkey; // Gemeinsamer geheimer Schlüssel zur Authentifizierung
    private final StartWindowAccess startWindowAccess; // Listener zur Benachrichtigung über Verbindungsstatus
    private final int PORT = 5000; // Fester Port für die Verbindung
    private final IOAccessReceiver receiver; // IOAccessReceiver für die Kommunikation mit SratWindow
    private String remoteIp; // IP-Adresse des Kommunikationspartners
    private Socket socket; // Verwendeter Socket für die Kommunikation
    private ServerSocket serverSocket; // ServerSocket für eingehende Verbindungen
    private IOManager ioManager; // IOManager für die Kommunikation


    public ConnectionManager(Boolean isHost, String remoteIp, String passkey, StartWindowAccess startWindowAccess, IOAccessReceiver receiver) {
        this.isHost = isHost;
        this.remoteIp = remoteIp;
        this.passkey = passkey;
        this.startWindowAccess = startWindowAccess;
        this.receiver = receiver;
    }

    // Validiert die Eingaben vor dem Verbindungsaufbau
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

    // Startet den Verbindungsaufbau in einem neuen Thread
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

    // Schliesst die bestehende Verbindung oder den ServerSocket
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

    // Startet einen ServerSocket und wartet auf eingehende Verbindungen
    private void startHost() throws Exception {
        startWindowAccess.onStatusUpdate("Starte Host...");
        serverSocket = new ServerSocket(PORT);
        startWindowAccess.onStatusUpdate("Host gestartet");
        while(true) {
            ConnectionState.setState(ConnectionState.WAITING); // Setze den Verbindungsstatus auf WAITING
            startWindowAccess.onConnecting(); // Benachrichtige über den Verbindungsstatus
            startWindowAccess.onStatusUpdate("Warte auf eingehende Verbindung...");
            socket = serverSocket.accept(); // Blockiert bis eine Verbindung eingeht
            ConnectionState.setState(ConnectionState.CONNECTING); // Setze den Verbindungsstatus auf CONNECTING
            startWindowAccess.onStatusUpdate("Anfrage von " + socket.getInetAddress().getHostAddress());
            startWindowAccess.onStatusUpdate("Empfange Passkey...");

            if (PasskeyManager.verifyPasskey(socket, passkey, isHost)) {
                startWindowAccess.onStatusUpdate("Passkey gültig");
                startIOManager();
                ConnectionState.setState(ConnectionState.CONNECTED); // Setze den Verbindungsstatus auf CONNECTED
                startWindowAccess.onConnected(); // Benachrichtige über erfolgreiche Verbindung
                break; // Verbindung akzeptiert, beende Warteschleife
            } else {
                ConnectionState.setState(ConnectionState.FAILED); // Setze den Verbindungsstatus auf FAILED
                startWindowAccess.onConnectionFailed("Ungültiger Passkey. Verbindung abgelehnt");
                socket.close(); // Verbindung schließen und auf nächste warten
            }
        }
    }

    // Baut eine Verbindung als Client zu einem Host auf
    private void startClient() throws Exception {
        try {
            ConnectionState.setState(ConnectionState.CONNECTING); // Setze den Verbindungsstatus auf CONNECTING
            startWindowAccess.onConnecting(); // Benachrichtige über den Verbindungsstatus
            startWindowAccess.onStatusUpdate("Versuche Verbindung zu " + remoteIp + "...");
            Socket newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(remoteIp, PORT), 10000); // 10 Sekunden Verbindungs-Timeout
            socket = newSocket;

            startWindowAccess.onStatusUpdate("Verbindung erfolgreich");
            startWindowAccess.onStatusUpdate("Sende Passkey...");

            if (PasskeyManager.verifyPasskey(socket, passkey, isHost)) {
                startWindowAccess.onStatusUpdate("Passkey bestätigt");
                startIOManager();
                ConnectionState.setState(ConnectionState.CONNECTED);
                startWindowAccess.onConnected(); // Benachrichtige über erfolgreiche Verbindung
            } else {
                ConnectionState.setState(ConnectionState.FAILED);
                startWindowAccess.onConnectionFailed("Ungültiger Passkey. Verbindung fehlgeschlagen.");
                socket.close();
                socket = null; // Setze den Socket auf null, um eine neue Verbindung zu ermöglichen
            }
        } catch (IOException e) {
            if (ConnectionState.getState() == ConnectionState.ABORTED) {
                return;
            }
            startWindowAccess.onConnectionFailed("Verbindungsversuch fehlgeschlagen: Host ist nicht erreichbar!");
            ConnectionState.setState(ConnectionState.FAILED);
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Schließe den Socket, wenn er existiert
                socket = null; // Setze den Socket auf null, um eine neue Verbindung zu ermöglichen
            }
        }
    }

    // Starte IOManager
    private void startIOManager() {
        try {
            String passkey = this.passkey;
            Boolean isHost = this.isHost;
            startWindowAccess.onStatusUpdate("Starte Chat...");
            ioManager = new IOManager(socket, passkey, isHost, startWindowAccess);
            ioManager.startCommunicationThreads(); // Starte Threads für Kommunikation
            startWindowAccess.onStatusUpdate("Chat gestartet");
        } catch (Exception e) {
            startWindowAccess.onConnectionFailed("Fehler beim Starten des IOManagers: " + e.getMessage());
        }
    }
}