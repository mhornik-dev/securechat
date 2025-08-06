package com.securechat;

import javax.swing.SwingUtilities;

import com.securechat.gui.StartWindow;

//TODO: Code optimieren
//TODO: Alle Variablen in den Klassen deklarieren
//TODO: Interfaces ausbauen

public class Main {
    public static void main(String[] args) {
        // Startet die GUI-Anwendung im Event-Dispatch-Thread
        SwingUtilities.invokeLater(() -> {
            // Erstellt das Startfenster der Anwendung
            StartWindow startWindow = new StartWindow();
            // Macht das Fenster sichtbar
            startWindow.setVisible(true);
        });
    }
}