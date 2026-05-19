# Client-Server-Project(Java) 🚀

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![UI Framework](https://img.shields.io/badge/UI-JavaFX-blue.svg)](https://openjfx.io/)
[![Protocol](https://img.shields.io/badge/Protocol-BUKA%20v1.0-green.svg)]()

A robust, multi-threaded Client-Server system implemented in Java that handles user authentication, real-time directory listing, and multi-format media streaming. Using a custom application-layer protocol (**BUKA**), the system cleanly transitions between plain-text command states and raw binary data pipelines to seamlessly transfer documents (`.pdf`) and audio tracks (`.mp3`).

---

## 🏗️ System Architecture & Data Flow

The system uses a strict protocol sequence over TCP sockets. To avoid desynchronization or stream corruption during binary file transfers, the pipeline shifts processing modes depending on the command layer:

```text
  [ Client UI ]                                       [ Server Engine ]
        │                                                     │
        │ ─── (TEXT) 1. LOGIN username password ───────────> │ (Validates text file)
        │ <── (TEXT) 2. <200> LOGGED IN ───────────────────── │
        │                                                     │
        │ ─── (TEXT) 3. LIST ──────────────────────────────> │ (Parses manifest file)
        │ <── (TEXT) 4. id1 name1#id2 name2# ──────────────── │
        │                                                     │
        │ ─── (TEXT) 5. PDFRET [File ID] ──────────────────> │ (Resolves filename)
        │ <── (TEXT) 6. [File Size in Bytes]\n ────────────── │ (Sends file size line)
        │ <── (BIN)  7. [Raw Byte Payload Stream] ─────────── │ (Switches to raw binary)
        v                                                     v
