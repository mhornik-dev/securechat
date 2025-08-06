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


    // Konstruktor, der die Instanzen initialisiert
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

    // Initialisiert die GUI-Komponenten
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

    // Getter für Input-Text
    public String getInputText() {
        return inputField.getText().trim();
    }

    // Methode zum Zurücksetzen des Eingabefelds
    public void clearInput() {
        inputField.setText("");
    }

    // Methode zum Anhängen einer Nachricht im Chatbereich
    public void appendMessage(String text, Color color) {
        StyledDocument doc = chatArea.getStyledDocument();
        Style style = chatArea.addStyle("Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text + "\n", style);
            chatArea.setCaretPosition(doc.getLength()); // Scroll automatisch ans Ende
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // Methode, die aufgerufen wird, wenn das Fenster geschlossen wird
    @Override
    public void onWindowClosing() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Willst du den Chat wirklich beenden? Dadurch wird die Verbindung automatisch getrennt.",
                "Chat beenden",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {   
            dispose(); // Schließt das Fenster
            startWindowAccess.onStatusUpdate("Chat wurde beendet");
            startWindowAccess.onDisconnected(); // Informiere den Listener über die Trennung   
            
        }
    }
}
