package com.securechat.gui;

// Interface zur Reaktion auf Verbindungsereignisse
public interface StartWindowAccess {

    // Wird aufgerufen, um Statusmeldungen weiterzugeben (z. B. für GUI-Anzeige)
    void onStatusUpdate(String message);

    // Wird aufgerufen, um den Verbindungsstatus zu aktualisieren (z. B. für GUI-Anzeige)
    void onConnecting();

    // Wird aufgerufen, wenn die Verbindung erfolgreich hergestellt wurde
    void onConnected();

    // Wird aufgerufen, wenn die Verbindung getrennt wird
    void onDisconnected();

    //Wird aufgerufen, wenn der Remote-Host/Client die Verbindung trennt (z. B. durch Schließen des Fensters)
    void onRemoteDisconnect();

    // Wird aufgerufen, wenn der Verbindungsaufbau fehlschlägt
    void onConnectionFailed(String error);

    // Wird aufgerufen, wenn der Verbindungsaufbau abgebrochen wird
    void onConnectionAborted();
}