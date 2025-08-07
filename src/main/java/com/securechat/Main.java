/**
 * Einstiegspunkt der Anwendung.
 * <p>
 * Diese Klasse enthält die {@code main}-Methode, welche die Anwendung initialisiert,
 * indem sie das {@link StartWindow} im Event-Dispatch-Thread startet und sichtbar macht.
 * 
 * @author Milos Hornik
 */
package com.securechat;

import javax.swing.SwingUtilities;
import com.securechat.gui.StartWindow;

public class Main {
    /**
     * Startet die Anwendung indem das Startfenster erstellt und sichtbar gemacht wird.
     * Diese Methode wird im Event-Dispatch-Thread ausgeführt, um sicherzustellen,
     * dass die GUI-Komponenten korrekt initialisiert und verwaltet werden.
     *
     * @param args Programmargumente
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StartWindow startWindow = new StartWindow();
            startWindow.setVisible(true);
        });
    }
}