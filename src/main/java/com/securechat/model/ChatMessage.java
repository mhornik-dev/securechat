/**
 * Repräsentiert eine Chat-Nachricht.
 * <p>
 * Die Klasse {@code ChatMessage} kapselt alle relevanten Metadaten einer Chat-Nachricht,
 * darunter Typ (immer "CHAT"), Nachrichtentext, Absender-IP und Zeitstempel.
 * Sie kann sowohl beim Versenden als auch beim Empfangen verwendet werden.
 * 
 * @author Milos Hornik
 */
package com.securechat.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String type;
    private String text;
    private String senderIp;
    private String timestamp;

    /**
     * Erstellt eine neue ChatMessage beim Versenden.
     * Der Zeitstempel wird automatisch gesetzt.
     *
     * @param text      Nachrichtentext
     * @param senderIp  IP-Adresse des Absenders
     */
    public ChatMessage(String text, String senderIp) {
        this.type = "CHAT"; // Setzt den Typ der Nachricht auf "CHAT"
        this.text = text;
        this.senderIp = senderIp;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Erstellt eine neue ChatMessage beim Empfangen.
     * Der Zeitstempel wird übernommen.
     *
     * @param text      Nachrichtentext
     * @param senderIp  IP-Adresse des Absenders
     * @param timestamp Zeitstempel der Nachricht
     */
    public ChatMessage(String text, String senderIp, String timestamp) {
        this.type = "CHAT";
        this.text = text;
        this.senderIp = senderIp;
        this.timestamp = timestamp; 
    }

    /**
     * Gibt den Typ der Nachricht zurück.
     * 
     * @return Nachrichtentyp (immer "CHAT")
     */
    public String getType() {
        return type;
    }

    /**
     * Gibt den Nachrichtentext zurück.
     * 
     * @return Text der Nachricht
     */
    public String getText() {
        return text;
    }

    /**
     * Gibt die IP-Adresse des Absenders zurück.
     * 
     * @return Absender-IP
     */
    public String getSenderIp() {
        return senderIp;
    }

    /**
     * Gibt den Zeitstempel der Nachricht zurück.
     * 
     * @return Zeitstempel als String
     */
    public String getTimestamp() {
        return timestamp;
    }
}