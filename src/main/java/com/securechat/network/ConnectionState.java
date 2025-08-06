package com.securechat.network;

public enum ConnectionState {
    DISCONNECTED,
    WAITING,
    CONNECTING,
    CONNECTED,
    ABORTED,
    FAILED;
    

    private static volatile ConnectionState currentState = DISCONNECTED;

    public static void setState(ConnectionState newState) {
        currentState = newState;
    }

    public static ConnectionState getState() {
        return currentState;
    }
}