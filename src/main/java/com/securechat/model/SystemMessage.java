package com.securechat.model;

public class SystemMessage {
    private String type;        // Typ der Nachricht (z. B. "SYSTEM")
    private String subtype;     // Untertyp der Nachricht (z. B. "REMOTESTATE", "PASSKEY")
    private String payload;     // Der eigentliche Payload
    private String senderIp;    // IP-Adresse des Absenders

    public SystemMessage(String subtype, String payload, String senderIp) {
        this.type = "SYSTEM";
        this.subtype = subtype;
        this.payload = payload;
        this.senderIp = senderIp;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getPayload() {
        return payload;
    }

    public String getSenderIp() {
        return senderIp;
    }
}
