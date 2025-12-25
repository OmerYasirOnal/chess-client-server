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
2. Download the chess-client-1.5.2.jar file
3. Run the client using the commands provided in the "Running the Application" section

## Usage
To play the game, follow these steps:
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

You can also play over the internet by connecting to a remote server:
1. Connect to the remote server:
   ```bash
  ./run-client.sh
   ```
2. In the login screen:
   - Enter your username
   - Use the server IP `141.147.25.123` with port `9999`
   - Click "Connect"

## Configuration
The game can be configured using environment variables or command-line arguments. For example, you can change the server port by setting the `SERVER_PORT` environment variable:
```bash
SERVER_PORT=8080./run-server.sh
```
You can also configure the game settings, such as the board size or the piece movements, by modifying the `config.properties` file.

## Contributing
To contribute to the project, follow these steps:
1. Fork the repository
2. Make your changes and commit them
3. Open a pull request against the main branch

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments
- Java Swing for the GUI components
- Java Socket API for networking functionality 

## Technical Architecture
The project is built using Java and follows a client-server architecture:
- **Server**: Manages game logic, validates moves, and coordinates communication between players
- **Client**: Provides the graphical interface and handles user interaction
- **Communication**: Uses socket-based networking with ping/pong heartbeat mechanism for reliable connections
- **Connection Management**: Robust handling of client disconnections with automatic cleanup of inactive sessions

## Building the Project
To build the project, follow these steps:
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
java -jar target/chess-server-1.5.2.jar
```
The server runs on port 9999 by default.

### Starting the Client
```bash
# Using the provided script
./run-client.sh

# Or manually
java -jar target/chess-client-1.5.2.jar
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
This will create a DMG file at `release/macos/Chess Game-1.5.2.dmg`.