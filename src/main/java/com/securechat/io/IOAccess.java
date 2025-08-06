package com.securechat.io;

// Interface für IO-Zugriff, für Kommunikation von StartWindow zu IOManager
public interface IOAccess {
    void sendSystemMessage(String subtype, String payload);
    void closeChatWindow();
}
