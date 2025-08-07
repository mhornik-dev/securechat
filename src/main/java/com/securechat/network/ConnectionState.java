/**
 * Repräsentiert den Verbindungsstatus einer Netzwerkverbindung.
 * <p>
 * Das Enum {@code ConnectionState} enthält alle möglichen Zustände einer Verbindung.
 * Es bietet außerdem statische Methoden zur globalen Verwaltung des aktuellen Status.
 * 
 * @author Milos Hornik
 */
package com.securechat.network;

public enum ConnectionState {
    DISCONNECTED,
    WAITING,
    CONNECTING,
    CONNECTED,
    ABORTED,
    FAILED;

    private static volatile ConnectionState currentState = DISCONNECTED;

    /**
     * Setzt den aktuellen Verbindungsstatus.
     *
     * @param newState neuer Zustand
     */
    public static void setState(ConnectionState newState) {
        currentState = newState;
    }

    /**
     * Gibt den aktuellen Verbindungsstatus zurück.
     *
     * @return aktueller Zustand
     */
    public static ConnectionState getState() {
        return currentState;
    }
}