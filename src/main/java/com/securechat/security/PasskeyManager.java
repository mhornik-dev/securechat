package com.securechat.security;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class PasskeyManager {
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

