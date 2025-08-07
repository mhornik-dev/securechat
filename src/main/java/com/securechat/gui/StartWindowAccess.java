/**
 * Interface zur Reaktion auf Verbindungsereignisse.
 * <p>
 * Das Interface {@code StartWindowAccess} definiert Methoden, um auf verschiedene
 * Ereignisse beim Aufbau und Betrieb einer Netzwerkverbindung zu reagieren.
 * Es wird insbesondere vom Startfenster implementiert, um Statusmeldungen,
 * Verbindungsänderungen und Fehler an die grafische Oberfläche weiterzuleiten.
 * 
 * @author Milos Hornik
 */
package com.securechat.gui;

public interface StartWindowAccess {

    /**
     * Wird aufgerufen, um Statusmeldungen weiterzugeben (z. B. für GUI-Anzeige).
     * @param message Statusmeldung
     */
    void onStatusUpdate(String message);

    /**
     * Wird aufgerufen, um den Verbindungsstatus auf "Verbinde..." zu setzen.
     */
    void onConnecting();

    /**
     * Wird aufgerufen, wenn die Verbindung erfolgreich hergestellt wurde.
     */
    void onConnected();

    /**
     * Wird aufgerufen, wenn die Verbindung getrennt wird.
     */
    void onDisconnected();

    /**
     * Wird aufgerufen, wenn der Remote-Host/Client die Verbindung trennt (z. B. durch Schließen des Fensters).
     */
    void onRemoteDisconnect();

    /**
     * Wird aufgerufen, wenn der Verbindungsaufbau fehlschlägt.
     * @param error Fehlermeldung
     */
    void onConnectionFailed(String error);

    /**
     * Wird aufgerufen, wenn der Verbindungsaufbau abgebrochen wird.
     */
    void onConnectionAborted();
}