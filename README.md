# Network Chess Game
A multiplayer chess game implemented in Java with client-server architecture, allowing two players to play chess over a network connection.

## Current Version
v1.5.2 - Production code cleanup, enhanced security, and improved error handling. Removed debug output, added input validation, and improved thread safety.

## Features
- Full implementation of standard chess rules
- Network multiplayer functionality
- Simple and intuitive GUI with enhanced visual design
- In-game chat between players
- Move validation and game state tracking
- Intuitive drag and drop piece movement
- Auto-start when second player joins
- Robust connection handling and disconnection detection

## Installation
### macOS Installation
For macOS users, a DMG installer is available:
1. Download the `Chess Game-1.5.2.dmg` file from the release directory
2. Double-click the DMG file to open it
3. Drag the Chess Game icon to the Applications folder
4. Open the application from your Applications folder or Launchpad

### Manual Installation
For other platforms, you can run the application directly using the JAR files:
1. Ensure you have Java 17 or higher installed
2. Download the `chess-client-1.5.2.jar` file
3. Run the client using the following command:
```bash
java -jar chess-client-1.5.2.jar
```

## Usage
To play the game, follow these steps:
1. **Login**: Connect to the server with your username
2. **Create or Join a Game**: 
   - Create a new game from the lobby
   - Or join an existing game from the list
3. **Playing the Game**: 
   - The game starts automatically when the second player joins
   - White moves first
   - Make valid chess moves by clicking or dragging pieces
   - Chat with your opponent using the chat panel
4. **Game End**: 
   - Game ends by checkmate, stalemate, resignation, or disconnection
   - Return to lobby to start a new game

## Technical Architecture
The project is built using Java and follows a client-server architecture:
- **Server**: Manages game logic, validates moves, and coordinates communication between players
- **Client**: Provides the graphical interface and handles user interaction
- **Communication**: Uses socket-based networking with ping/pong heartbeat mechanism for reliable connections
- **Connection Management**: Robust handling of client disconnections with automatic cleanup of inactive sessions

## Requirements
- Java Development Kit (JDK) 17 or higher
- Maven for dependency management and building

## Building the Project
To build the project, run the following command:
```bash
mvn clean package
```

## Running the Application
### Starting the Server
To start the server, run the following command:
```bash
java -jar target/chess-server-1.5.2.jar
```
The server runs on port 9999 by default.

### Starting the Client
To start the client, run the following command:
```bash
java -jar target/chess-client-1.5.2.jar
```
When the client starts, you'll need to enter:
- Your username
- Server address (localhost or remote IP)
- Port number (default: 9999)

## Building the macOS DMG
To build the macOS DMG installer, run the following command:
```bash
./build-macos-dmg.sh
```
This will create a DMG file at `release/macos/Chess Game-1.5.2.dmg`.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Contributors
- Omer Yasir Onal

## Acknowledgments
- Java Swing for the GUI components
- Java Socket API for networking functionality