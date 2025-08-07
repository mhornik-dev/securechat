/**
 * Interface für das Empfangen und Setzen eines {@link IOAccess}-Objekts.
 * <p>
 * Das Interface {@code IOAccessReceiver} dient als Callback, um einem Empfänger
 * die Möglichkeit zu geben, eine {@link IOAccess}-Instanz zu setzen – etwa im Startfenster.
 * 
 * @author Milos Hornik
 */
package com.securechat.io;

public interface IOAccessReceiver {
    /**
     * Setzt die {@link IOAccess}-Instanz.
     *
     * @param ioAccess zu setzendes IOAccess-Objekt
     */
    void setIOAccess(IOAccess ioAccess);
}