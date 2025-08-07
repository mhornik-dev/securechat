/**
 * Fenster für den Peer-to-Peer-Chat.
 * <p>
 * Die Klasse {@code ChatWindow} stellt das Hauptfenster für die Chat-Kommunikation dar.
 * Sie bietet ein Textfeld zur Anzeige des Chatverlaufs, ein Eingabefeld, sowie die Möglichkeit,
 * Nachrichten zu senden und darzustellen. Die Verwaltung der Chat-Logik erfolgt über einen
 * {@link IOManager}. Das Fenster reagiert auf Schließen-Events und informiert das Startfenster
 * über Statusänderungen.
 * 
 * @author Milos Hornik
 */
package com.securechat.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.securechat.io.IOManager;

import javax.swing.text.Style;

import java.awt.*;

//TODO: Unique ID für ChatWindow erstellen

public class ChatWindow extends JFrame implements WindowListener {
    private final IOManager ioManager;
    private final Boolean isHost;
    private final StartWindowAccess startWindowAccess;

    private JTextPane chatArea;
    private JTextField inputField;

    /**
     * Erstellt ein neues ChatWindow für Host oder Client.
     *
     * @param isHost             {@code true}, wenn das Fenster für den Host ist; sonst {@code false}
     * @param ioManager          IO-Manager für Nachrichtenübermittlung
     * @param startWindowAccess  Zugriff auf das Startfenster (Callbacks)
     */
    public ChatWindow(Boolean isHost, IOManager ioManager, StartWindowAccess startWindowAccess) {
        this.isHost = isHost;
        this.ioManager = ioManager;
        this.startWindowAccess = startWindowAccess;

        setTitle("P2P Chat - " + (isHost ? "Host" : "Client"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onWindowClosing();
            }
        });

        initComponents();
        setVisible(true);
    }

    /**
     * Initialisiert die GUI-Komponenten und das Layout des Fensters.
     */
    private void initComponents() {
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.addActionListener(e -> ioManager.sendChatMessage());

        JButton sendButton = new JButton("Senden");
        sendButton.addActionListener(e -> ioManager.sendChatMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Gibt den aktuellen Inhalt des Eingabefelds zurück.
     *
     * @return Text im Eingabefeld
     */
    public String getInputText() {
        return inputField.getText().trim();
    }

    /**
     * Setzt das Eingabefeld zurück.
     */
    public void clearInput() {
        inputField.setText("");
    }

    /**
     * Hängt eine Nachricht im Chatbereich an und färbt sie.
     *
     * @param text  Nachrichtentext
     * @param color Farbe für die Nachricht
     */
    public void appendMessage(String text, Color color) {
        StyledDocument doc = chatArea.getStyledDocument();
        Style style = chatArea.addStyle("Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text + "\n", style);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wird aufgerufen, wenn das Fenster geschlossen wird.
     * Bestätigt das Schließen und informiert das Startfenster über die Trennung.
     */
    @Override
    public void onWindowClosing() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Willst du den Chat wirklich beenden? Dadurch wird die Verbindung automatisch getrennt.",
                "Chat beenden",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {   
            dispose();
            startWindowAccess.onStatusUpdate("Chat wurde beendet");
            startWindowAccess.onDisconnected();   
        }
    }
}