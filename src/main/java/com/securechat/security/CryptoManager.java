/**
 * Verwaltet die symmetrische AES-Verschlüsselung und -Entschlüsselung.
 * <p>
 * Die Klasse {@code CryptoManager} erlaubt es, Strings mit einem geteilten Schlüssel
 * per AES zu verschlüsseln und zu entschlüsseln. Der Schlüssel wird intern auf 16 Bytes 
 * angepasst. Die verschlüsselten Daten werden Base64-kodiert übertragen.
 * 
 * @author Milos Hornik
 */
package com.securechat.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoManager {
    private final SecretKeySpec secretKey;

    /**
     * Erstellt einen CryptoManager mit dem angegebenen Schlüssel.
     * Der Schlüssel wird für AES auf 16 Bytes gebracht.
     *
     * @param key Das geheime Passwort
     */
    public CryptoManager(String key) {
        // Kürze oder erweitere Key auf 16 Byte für AES
        String fixedKey = String.format("%-16s", key).substring(0, 16);
        this.secretKey = new SecretKeySpec(fixedKey.getBytes(), "AES");
    }

    /**
     * Verschlüsselt die Eingabedaten per AES und gibt sie Base64-kodiert zurück.
     *
     * @param data Klartextdaten
     * @return Verschlüsselte und codierte Daten
     * @throws Exception bei Verschlüsselungsfehlern
     */
    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Entschlüsselt Base64-codierte AES-Daten.
     *
     * @param encryptedData Verschlüsselte Daten bzw. einen leeren String bei Fehler
     * @return Entschlüsselter Klartext oder leerer String bei Fehler
     * @throws Exception bei Entschlüsselungsfehlern
     */
    public String decrypt(String encryptedData) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            return "";
        }
    }
}