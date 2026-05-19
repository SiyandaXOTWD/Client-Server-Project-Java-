# Client-Server-Project(Java) 🚀

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![UI Framework](https://img.shields.io/badge/UI-JavaFX-blue.svg)](https://openjfx.io/)

A robust, multi-threaded Client-Server system implemented in Java that handles user authentication, real-time directory listing, and multi-format media streaming. Using a custom application-layer protocol, the system cleanly transitions between plain-text command states and raw binary data pipelines to seamlessly transfer documents (`.pdf`) and audio tracks (`.mp3`).

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


📂 Repository Structure

```text
[Structure]
├── src/
│   ├── ClientServer/
│   │   ├── server/
│   │   │   ├── Server.java            # Main server listener (Socket bind)
│   │   │   └── ServerHandler.java       # Multi-threaded connection worker (Runnable)
│   │   └── client/
│   │       ├── Client.java            # Main client application entry point (JavaFX App)
│   │       └── ClientPane.java    # JavaFX layout view and client event handlers
│   
└── data/
    ├── server/
    │   ├── users.txt                  # Authorized user credentials file
    │   ├── PdfList.txt                # Numeric key to file mapping manifest
    │   └── Tshego-Garden.mp3          # Host media assets folder
    └── client/
        └── [Downloaded Files]         # Destination directory for completed transfers


🛠️ Configuration & Setup
1. Server Environment Setup
Before starting the server, ensure that your data/server/ directory is populated with your files and matching flat-file databases.
- users.txt Configuration (Format: [username] [password])
 Example: Riri 123
- PdfList.txt Configuration (Format: [ID] [Exact_Filename])
 Example: 1 ihavedream.pdf

2. Compiling and Execution
Step 1: Start the Server Host
Run the server package entry point to spin up the port listener: java ClientServer.server.Server
Console Confirmation Output: Server connected on: 2018
Step 2: Launch the Client Application
Execute the main client driver class to start the JavaFX stage: java csc2b.client.Client

🕹️ Application Workflow Guide
Log In: Provide a matching credential set found in users.txt(Riri 123) and click LOG IN.
Fetch Catalog: Click the LIST button to pull down the available index from the server.
Stream Media File: * Identify the Numeric ID associated with your file (e.g., enter 4 to request Tshego-Garden.mp3).
Type the ID into the download text field and click DOWNLOAD.
Once the byte transfer matches the server's tracking constraints, the system will update with:
FILE DOWNLOADED: Tshego-Garden.mp3
