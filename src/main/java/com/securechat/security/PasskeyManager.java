/**
 * Verwaltet die Verifizierung des Passworts (Passkey) zwischen zwei Chat-Teilnehmern.
 * <p>
 * Die Klasse {@code PasskeyManager} bietet eine Methode, um einen Passkey 
 * zwischen Host und Client auszutauschen und zu verifizieren.
 * Die Kommunikation erfolgt verschlüsselt über den {@link CryptoManager}.
 * Abhängig von der Rolle (Host oder Client) wird der Passkey gesendet bzw. geprüft.
 * 
 * @author Milos Hornik
 */
package com.securechat.security;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class PasskeyManager {

    /**
     * Überprüft den Passkey zwischen Host und Client über einen Socket.
     * Die Kommunikation erfolgt verschlüsselt.
     *
     * @param socket   Der Socket für die Kommunikation.
     * @param passkey  Das zu überprüfende Passwort.
     * @param isHost   {@code true}, wenn diese Instanz als Host agiert, {@code false} für Client.
     * @return {@code true}, wenn die Verifizierung erfolgreich war, sonst {@code false}.
     * @throws Exception bei Netzwerk- oder Krypto-Fehlern.
     */
    public static boolean verifyPasskey(Socket socket, String passkey, boolean isHost) throws Exception {
        CryptoManager crypto = new CryptoManager(passkey);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        if (isHost) {
            String encryptedReceived = in.readLine();
            String received = crypto.decrypt(encryptedReceived);
            if (received.equals(passkey)) {
                out.println(crypto.encrypt("VALID"));
                return true;
            } else {
                out.println(crypto.encrypt("NOT VALID"));
                return false;
            }
        } else {
            out.println(crypto.encrypt(passkey));
            String response = crypto.decrypt(in.readLine());
            return response.equals("VALID");
        }
    }
}