/**
 * Repräsentiert eine systeminterne Nachricht.
 * <p>
 * Die Klasse {@code SystemMessage} wird für spezielle Nachrichten zwischen den Teilnehmern verwendet,
 * etwa zur Übertragung von Statusinformationen oder Steuerdaten. Jede SystemMessage besitzt einen Typ,
 * einen Subtyp, einen frei definierbaren Payload sowie die IP-Adresse des Absenders.
 * 
 * @author Milos Hornik
 */
package com.securechat.model;

public class SystemMessage {
    private String type;        
    private String subtype;
    private String payload;
    private String senderIp;

    /**
     * Erstellt eine neue SystemMessage mit festem Typ "SYSTEM".
     *
     * @param subtype  Untertyp der Nachricht (z. B. "REMOTESTATE")
     * @param payload  Inhalt bzw. Nutzdaten der Nachricht
     * @param senderIp IP-Adresse des Absenders
     */
    public SystemMessage(String subtype, String payload, String senderIp) {
        this.type = "SYSTEM";
        this.subtype = subtype;
        this.payload = payload;
        this.senderIp = senderIp;
    }

    /**
     * Gibt den Typ der Nachricht zurück.
     * 
     * @return Nachrichtentyp (immer "SYSTEM")
     */
    public String getType() {
        return type;
    }

    /**
     * Gibt den Subtyp der Nachricht zurück.
     * 
     * @return Subtyp (z. B. "REMOTESTATE")
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Gibt den Payload der Nachricht zurück.
     * 
     * @return Nutzdaten
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Gibt die IP-Adresse des Absenders zurück.
     * 
     * @return Absender-IP
     */
    public String getSenderIp() {
        return senderIp;
    }
}