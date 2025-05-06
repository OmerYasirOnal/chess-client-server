# Network Chess Game

A multiplayer chess game implemented in Java with client-server architecture, allowing two players to play chess over a network connection.

## Current Version
v1.5.0 - Removed time control mechanism and implemented auto-start game feature. Games now start automatically when a second player joins, without requiring ready status.

## Features

- Full implementation of standard chess rules
- Network multiplayer functionality
- Simple and intuitive GUI with enhanced visual design
- In-game chat between players
- Move validation and game state tracking
- Intuitive drag and drop piece movement
- Auto-start when second player joins

## Installation

### macOS Installation
For macOS users, a DMG installer is available:
1. Download the `Chess Game-1.5.0.dmg` file from the release directory
2. Double-click the DMG file to open it
3. Drag the Chess Game icon to the Applications folder
4. Open the application from your Applications folder or Launchpad

### Manual Installation
For other platforms, you can run the application directly using the JAR files:
1. Ensure you have Java 17 or higher installed
2. Download the chess-client-1.5.0.jar file
3. Run the client using the commands provided in the "Running the Application" section

## How to Play with Multiple Clients

You can play the game with two chess clients, either on the same computer or on different computers:

### Option 1: Playing on the Same Computer (Local Testing)

1. Start the server:
   ```bash
   ./run-server-localhost.sh
   ```

2. Start the first client:
   ```bash
   ./run-client-localhost.sh
   ```
   
3. Start the second client:
   ```bash
   ./run-client-localhost.sh
   ```

4. In each client:
   - Enter a different username
   - Connect to `localhost` with port `9999`
   - The first player creates a game, the second player joins it

### Option 2: Playing Over the Internet

1. Connect to the remote server:
   ```bash
   ./run-client.sh
   ```

2. In the login screen:
   - Enter your username
   - Use the server IP `141.147.25.123` with port `9999`
   - Click "Connect"

3. Follow the same steps on another computer to join the game

## Game Flow

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
- **Communication**: Uses socket-based networking for real-time gameplay

## Requirements

- Java Development Kit (JDK) 17 or higher
- Maven for dependency management and building

## Building the Project

```bash
# Clone the repository
git clone https://github.com/OmerYasirOnal/chess-client-server.git
cd chess-client-server

# Build with Maven
mvn clean package
```

## Running the Application

### Starting the Server

```bash
# Using the provided script
./run-server.sh

# Or manually
java -jar target/chess-server-1.5.0.jar
```

The server runs on port 9999 by default.

### Starting the Client

```bash
# Using the provided script
./run-client.sh

# Or manually
java -jar target/chess-client-1.5.0.jar
```

When the client starts, you'll need to enter:
- Your username
- Server address (localhost or remote IP)
- Port number (default: 9999)

## Building the macOS DMG

To build the macOS DMG installer:

```bash
# Make sure you have JDK 14+ with jpackage installed
./build-macos-dmg.sh
```

This will create a DMG file at `release/macos/Chess Game-1.5.0.dmg`.

## Screenshots

Screenshots of the game can be found in the `src/main/resources/screenshots` directory.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributors

- Omer Yasir Onal

## Acknowledgments

- Java Swing for the GUI components
- Java Socket API for networking functionality 