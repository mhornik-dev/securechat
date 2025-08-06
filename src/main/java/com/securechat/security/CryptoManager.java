package com.securechat.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoManager {
    private final SecretKeySpec secretKey;

    // Konstruktor: Initialisiert den AES-Schlüssel
    public CryptoManager(String key) {
        // Kürze oder erweitere Key auf 16 Byte für AES
        String fixedKey = String.format("%-16s", key).substring(0, 16);
        this.secretKey = new SecretKeySpec(fixedKey.getBytes(), "AES");
    }

    // Verschlüsselt die Eingabedaten
    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Entschlüsselt die verschlüsselten Daten
    public String decrypt(String encryptedData) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            return ""; // Gibt leeren String zurück bei Fehler
        }
    }
}
