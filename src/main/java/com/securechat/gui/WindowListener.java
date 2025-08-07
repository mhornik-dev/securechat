/**
 * Interface zur Reaktion auf das Schließen eines Fensters.
 * <p>
 * Das Interface {@code WindowListener} definiert eine Methode, die aufgerufen wird,
 * wenn ein Fenster geschlossen wird. Es dient als Callback für GUI-Komponenten,
 * um auf das Schließen gezielt reagieren zu können.
 * 
 * @author Milos Hornik
 */
package com.securechat.gui;

public interface WindowListener {

    /**
     * Wird aufgerufen, wenn ein Fenster geschlossen wird.
     */
    void onWindowClosing();

}