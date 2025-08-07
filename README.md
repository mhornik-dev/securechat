# SecureChat - P2P Encryption Messenger

**SecureChat** ist eine Java-basierte Peer-to-Peer-Chat-Anwendung, die Wert auf Sicherheit, Verschlüsselung und einfache Bedienung legt.  
Zwei Nutzer können sich direkt verbinden, verschlüsselt chatten und ihre Nachrichten in einer modernen GUI austauschen.

---

## 🔒 Features

- **Verschlüsselte P2P-Kommunikation**
- **Intuitive Swing-GUI**
- **Host- oder Client-Modus**
- **Trennung & Statuswechsel**
- **Farbliche, formatierte Nachrichtenanzeige**
- **Passkey-basierte Authentifizierung**
- **Modular & erweiterbar**

---

## 🏗️ Projektstruktur

```
securechat/
├── gui/            # GUI-Klassen
├── io/             # IO-Management
├── model/          # Datenmodelle
├── network/        # Verbindungslogik
├── security/       # Kryptografie & Passkey-Handling
└── Main.java       # Einstiegspunkt
```

---

## ✨ Architektur-Überblick

- **StartWindow:** Hauptfenster für Verbindungsaufbau, Status, Trennung
- **ChatWindow:** Modernes Chat-Fenster mit farbigen Nachrichten
- **IOManager:** Verwaltung der verschlüsselten Kommunikation (Senden, Empfangen, Threads)
- **ConnectionManager:** Baut Verbindungen im Host- oder Client-Modus auf
- **Modulare Interfaces:** Für künftige Erweiterungen (z. B. mehrere Connections)

---

## ⌨️ Bedienung

| Aktion                | Beschreibung                |
|-----------------------|----------------------------|
| Host/Client wählen    | Checkbox im Startfenster   |
| IP & Passkey eingeben | Felder im Startfenster     |
| Verbindung starten    | "Verbindung starten"-Button|
| Trennen               | "Verbindung trennen"-Button|
| Nachricht senden      | Eingabefeld + Enter/Button |
| Beenden               | Fenster schließen          |

---

**Viel Spaß beim sicheren Chatten!**
