/**
 * Interface für den IO-Zugriff.
 * <p>
 * Das Interface {@code IOAccess} definiert die Methoden für die Kommunikation 
 * zwischen Startfenster und IO-Manager. Es ermöglicht das Senden von Systemnachrichten 
 * und das Schließen des Chat-Fensters.
 * 
 * @author Milos Hornik
 */
package com.securechat.io;

public interface IOAccess {

    /**
     * Sendet eine Systemnachricht mit Subtyp und Nutzdaten.
     *
     * @param subtype  Subtyp der Nachricht
     * @param payload  Nutzdaten
     */
    void sendSystemMessage(String subtype, String payload);

    /**
     * Schließt das Chat-Fenster.
     */
    void closeChatWindow();
}