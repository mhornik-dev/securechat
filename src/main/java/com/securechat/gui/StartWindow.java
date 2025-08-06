package com.securechat.gui;

import javax.swing.*;

import com.securechat.io.IOAccess;
import com.securechat.io.IOAccessReceiver;
import com.securechat.network.ConnectionManager;
import com.securechat.network.ConnectionState;

import java.awt.*;
import java.awt.event.*;

//TODO: Möglichkeit mehrere Verbindungen (ConnectionManager) zu erstellen implementieren

public class StartWindow extends JFrame implements StartWindowAccess, IOAccessReceiver, WindowListener {

    private JTextField ipField;
    private JTextField passkeyField;
    private JTextArea statusArea;
    private JCheckBox hostCheck;
    private JCheckBox clientCheck;
    private JButton connectButton;
    private JButton disconnectButton;
    private Boolean isHost;
    private Boolean isClient;

    private volatile ConnectionManager manager;
    private IOAccess ioAccess;
    private JLabel logoLabel;
   
    
    // Konstruktor, der die Instanzen initialisiert
    public StartWindow() {
        setTitle("P2P Chat - Start");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Fügt einen WindowListener hinzu, um auf das Schließen des Fensters zu reagieren
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing();
            }
        });

        // Initialisiert die GUI-Komponenten
        initComponents();
        setVisible(true);
    }

    // Initialisiert die GUI-Komponenten
    private void initComponents() {

        ConnectionState state = ConnectionState.getState();
        ImageIcon logo = getLogo(state);     

        // Erstellen des Logo-Labels
        logoLabel = new JLabel(logo);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel panel = new JPanel(new GridLayout(8, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ipField = new JTextField();
        passkeyField = new JTextField();

        hostCheck = new JCheckBox("Host");
        clientCheck = new JCheckBox("Client");

        ButtonGroup group = new ButtonGroup();
        group.add(hostCheck);
        group.add(clientCheck);

        hostCheck.addActionListener(e -> ipField.setEnabled(false));
        clientCheck.addActionListener(e -> ipField.setEnabled(true));

        connectButton = new JButton("Verbindung starten");
        disconnectButton = new JButton("Verbindung trennen");
        disconnectButton.setEnabled(false);


        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(statusArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        scroll.setPreferredSize(new Dimension(380, 100));

        panel.add(new JLabel("Host IP:"));
        panel.add(ipField);
        panel.add(new JLabel("Passkey:"));
        panel.add(passkeyField);
        panel.add(hostCheck);
        panel.add(clientCheck);
        panel.add(connectButton);
        panel.add(disconnectButton);

        add(logoLabel, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // ActionListener für connectButton
        connectButton.addActionListener(e -> {actionConnectButton();});

        // ActionListener für disconnectButton
        disconnectButton.addActionListener(e -> {actionDisconnectButton();});
    }

    /* ActionListener-Methoden */

    // Action für Connect-Button
    private void actionConnectButton() {
        isHost = hostCheck.isSelected();
        isClient = clientCheck.isSelected();
        String ip = ipField.getText().trim();
        String passkey = passkeyField.getText().trim();
        if (ConnectionManager.prepareConnection(isHost, isClient, ip, passkey, this)) {
            manager = new ConnectionManager(isHost, ip, passkey, this, this);
            manager.startConnection();
            connectGUIState();
        }
    }

    // Action für Disconnect-Button
    private void actionDisconnectButton() {
        if (ioAccess != null) {
            ioAccess.closeChatWindow();
        }
        onDisconnected();
    }

    /* GUI-Zustände */

    // GUI-Zustand Connected
    public void connectGUIState() {
        connectButton.setEnabled(false);
        ipField.setEnabled(false);
        passkeyField.setEnabled(false);
        hostCheck.setEnabled(false);
        clientCheck.setEnabled(false);
        disconnectButton.setEnabled(true);
    }

    // GUI-Zustand Disconnected
    public void disconnectGUIState() {
        connectButton.setEnabled(true);
        ipField.setEnabled(isClient);
        passkeyField.setEnabled(true);
        hostCheck.setEnabled(true);
        hostCheck.setSelected(isHost);
        clientCheck.setEnabled(true);
        clientCheck.setSelected(isClient);
        disconnectButton.setEnabled(false);
    }

    /* Logo-Methoden */

    // Methode, die das Logo basierend auf dem Verbindungsstatus zurückgibt
    private ImageIcon getLogo(ConnectionState state) {
        switch (state) {
            case CONNECTED:
                return new ImageIcon(StartWindow.class.getResource("/images/logo_connected.png"));
            case CONNECTING:
            case WAITING:
                return new ImageIcon(StartWindow.class.getResource("/images/logo_waiting.png"));
            case FAILED:
                return new ImageIcon(StartWindow.class.getResource("/images/logo_failed.png"));
            default:
                return new ImageIcon(StartWindow.class.getResource("/images/logo.png"));
        }
    }

    // Methode, die das Logo aktualisiert
    private void updateLogo(ConnectionState state) {
        logoLabel.setIcon(getLogo(state));

    }

    /* ConnectionListener-Methoden */

    // Methode, die aufgerufen wird, wenn das Fenster geschlossen wird
    @Override
    public void onStatusUpdate(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }

    // Methode, die aufgerufen wird, wenn die Verbindung hergestellt wird
    @Override
    public void onConnecting() {
        SwingUtilities.invokeLater(() -> {
            updateLogo(ConnectionState.CONNECTING);
        });
    }

    // Methode, die aufgerufen wird, wenn die Verbindung erfolgreich hergestellt wurde
    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            updateLogo(ConnectionState.CONNECTED);
        });
    }

    // Methode, die aufgerufen wird, wenn die Verbindung getrennt wird
    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            // Senden der Disconnect-Nachricht an den IOAccess
            if (ioAccess != null) {
                ioAccess.sendSystemMessage("REMOTESTATE", "DISCONNECT");
            }
            // Schließen der Verbindung
            if (manager != null) {
                manager.closeConnection();
                manager = null;
            }
            updateLogo(ConnectionState.DISCONNECTED);
            disconnectGUIState();
            
        });
    }

    // Methode, die aufgerufen wird, wenn der Remote-Host/Client die Verbindung trennt
    @Override
    public void onRemoteDisconnect() {
        SwingUtilities.invokeLater(() -> {
            if (manager != null) {
                manager.closeConnection();
                manager = null;
                ConnectionState.setState(ConnectionState.DISCONNECTED);
            }
            onStatusUpdate("Verbindung wurde getrennt.");
            updateLogo(ConnectionState.DISCONNECTED);
            disconnectGUIState();
        });
    }

    // Methode, die aufgerufen wird, wenn der Verbindungsaufbau fehlschlägt
    @Override
    public void onConnectionFailed(String error) {
        SwingUtilities.invokeLater(() -> {
            onStatusUpdate(error);
            if (!isHost) {
                disconnectGUIState();
                updateLogo(ConnectionState.FAILED);
            }
            
                       
        });
    }

    // Methode, die aufgerufen wird, wenn der Verbindungsaufbau abgebrochen wird
    @Override
    public void onConnectionAborted() {
        SwingUtilities.invokeLater(() -> {
            onStatusUpdate("Verbindungsversuch abgebrochen.");
            disconnectGUIState();
        });
    }

    /* WindowListener-Methoden */

    // Methode, die aufgerufen wird, wenn das Fenster geschlossen wird
    @Override
    public void onWindowClosing() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Willst du das Programm wirklich beenden?",
                "Programm beenden",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            if (manager != null) {
                manager.closeConnection();
            }
            System.exit(0);
        }
    }

    /* IOAccessReceiver-Methoden */

    // IOAccess-Interface zuweisen, nachdem der IOManager im ConnectionManager erstellt wurde
    @Override
    public void setIOAccess(IOAccess ioAccess) {
        this.ioAccess = ioAccess;

    }

    
}