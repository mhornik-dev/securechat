/**
 * Startfenster der Anwendung.
 * <p>
 * Die Klasse {@code StartWindow} stellt die grafische Oberfläche zum Aufbau und zur Verwaltung
 * der Peer-to-Peer-Chatverbindung bereit. Von hier aus können Nutzer die Verbindung als Host
 * oder Client initiieren, den Status überwachen sowie den Verbindungsauf- und -abbau steuern.
 * Zudem werden Statusmeldungen und dynamische Logos basierend auf dem Verbindungsstatus angezeigt.
 * 
 * Die Klasse implementiert die Interfaces {@link StartWindowAccess}, {@link IOAccessReceiver} 
 * und {@link WindowListener}, um Rückmeldungen und IO-Funktionen zu verarbeiten.
 * 
 * @author Milos Hornik
 */
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
   
    /**
     * Erstellt das Startfenster und initialisiert alle GUI-Komponenten.
     */
    public StartWindow() {
        setTitle("P2P Chat - Start");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing();
            }
        });
        initComponents();
        setVisible(true);
    }

    /**
     * Initialisiert die grafischen Komponenten des Fensters und setzt ActionListener.
     */
    private void initComponents() {

        ConnectionState state = ConnectionState.getState();
        ImageIcon logo = getLogo(state);     

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

        connectButton.addActionListener(e -> {actionConnectButton();});
        disconnectButton.addActionListener(e -> {actionDisconnectButton();});
    }

    /**
     * Aktion für den Connect-Button: Verbindungsaufbau einleiten.
     */
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

    /**
     * Aktion für den Disconnect-Button: Verbindung trennen.
     */
    private void actionDisconnectButton() {
        if (ioAccess != null) {
            ioAccess.closeChatWindow();
        }
        onDisconnected();
    }

    /**
     * Setzt die GUI auf den Status "Connected".
     */
    public void connectGUIState() {
        connectButton.setEnabled(false);
        ipField.setEnabled(false);
        passkeyField.setEnabled(false);
        hostCheck.setEnabled(false);
        clientCheck.setEnabled(false);
        disconnectButton.setEnabled(true);
    }

    /**
     * Setzt die GUI auf den Status "Disconnected".
     */
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

    /**
     * Liefert das Logo-Icon passend zum Verbindungsstatus.
     *
     * @param state aktueller Verbindungsstatus
     * @return passendes {@link ImageIcon}
     */
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

    /**
     * Aktualisiert das Logo-Icon je nach Verbindungsstatus.
     *
     * @param state neuer Verbindungsstatus
     */
    private void updateLogo(ConnectionState state) {
        logoLabel.setIcon(getLogo(state));
    }

    /**
     * Zeigt eine Statusmeldung im Statusbereich an.
     *
     * @param message anzuzeigende Nachricht
     */
    @Override
    public void onStatusUpdate(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append(message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }

    /**
     * Wird aufgerufen, wenn die Verbindung aufgebaut wird.
     * Aktualisiert das Logo und den GUI-Zustand.
     */
    @Override
    public void onConnecting() {
        SwingUtilities.invokeLater(() -> {
            updateLogo(ConnectionState.CONNECTING);
        });
    }

    /**
     * Wird aufgerufen, wenn die Verbindung erfolgreich aufgebaut wurde.
     * Aktualisiert das Logo und den GUI-Zustand.
     */
    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            updateLogo(ConnectionState.CONNECTED);
        });
    }

    /**
     * Wird aufgerufen, wenn die Verbindung getrennt wurde.
     * Beendet die Verbindung und aktualisiert den GUI-Zustand.
     */
    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            if (ioAccess != null) {
                ioAccess.sendSystemMessage("REMOTESTATE", "DISCONNECT");
            }
            if (manager != null) {
                manager.closeConnection();
                manager = null;
            }
            updateLogo(ConnectionState.DISCONNECTED);
            disconnectGUIState();
        });
    }

    /**
     * Wird aufgerufen, wenn der Remote-Host/Client die Verbindung trennt.
     * Beendet die Verbindung und aktualisiert den GUI-Zustand.
     */
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

    /**
     * Wird aufgerufen, wenn der Verbindungsaufbau fehlschlägt.
     * Zeigt eine Fehlermeldung an und aktualisiert den GUI-Zustand.
     *
     * @param error Fehlermeldung
     */
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

    /**
     * Wird aufgerufen, wenn der Verbindungsaufbau abgebrochen wird.
     * Zeigt eine Statusmeldung an und aktualisiert den GUI-Zustand.
     */
    @Override
    public void onConnectionAborted() {
        SwingUtilities.invokeLater(() -> {
            onStatusUpdate("Verbindungsversuch abgebrochen.");
            disconnectGUIState();
        });
    }

    /**
     * Wird aufgerufen, wenn das Fenster geschlossen werden soll (z.B. durch Nutzer).
     * Fragt nach Bestätigung und beendet das Programm.
     */
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

    /**
     * Setzt das {@link IOAccess}-Objekt nach Erstellung des {@link IOManager}.
     *
     * @param ioAccess Zugriffsschnittstelle für IO
     */
    @Override
    public void setIOAccess(IOAccess ioAccess) {
        this.ioAccess = ioAccess;
    }
}