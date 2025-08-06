package com.securechat.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String type;        // Typ der Nachricht (z. B. "CHAT")
    private String text;        // Der eigentliche Nachrichtentext
    private String senderIp;    // IP-Adresse des Absenders
    private String timestamp;   // Zeitstempel der Nachricht im Format "yyyy-MM-dd HH:mm:ss"

    // Konstruktor: erstellt eine neue Nachricht mit Metadaten (Beim Versenden)
    public ChatMessage(String text, String senderIp) {
        this.type = "CHAT"; // Setzt den Typ der Nachricht auf "CHAT"
        this.text = text;
        this.senderIp = senderIp;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Konstruktor: erstellt eine neue Nachricht mit Metadaten (Beim Empfangen)
    public ChatMessage(String text, String senderIp, String timestamp) {
        this.type = "CHAT"; // Setzt den Typ der Nachricht auf "CHAT"
        this.text = text;
        this.senderIp = senderIp;
        this.timestamp = timestamp; // Setzt den Zeitstempel auf den übergebenen Wert
    }

    // Getter für Type
    public String getType() {
        return type;
    }

    // Gibt den Nachrichtentext zurück
    public String getText() {
        return text;
    }

    // Gibt die IP-Adresse des Absenders zurück
    public String getSenderIp() {
        return senderIp;
    }

    // Gibt den Zeitstempel zurück
    public String getTimestamp() {
        return timestamp;
    }
}
