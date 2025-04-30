# Network Chess Game

A multiplayer chess game implemented in Java with client-server architecture, allowing two players to play chess over a network connection.

## Current Version
v1.4.0 - UI improvements, chess timer implementation, and drag and drop movement. See ChangeLog.txt for details.

## Features

- Full implementation of standard chess rules
- Network multiplayer functionality
- Simple and intuitive GUI with enhanced visual design
- In-game chat between players
- Move validation and game state tracking
- Fully functional chess timer with different time control options
- Intuitive drag and drop piece movement

## Coming in Version 1.5.0

- **Game History**: View and replay past games
- **Advanced Statistics**: Track your performance and improvement
- **Tournament Mode**: Create and participate in online tournaments

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
java -jar target/chess-server-1.4.0.jar [port]
```

The default port is 5000 if not specified.

### Starting the Client

```bash
java -jar target/chess-client-1.4.0.jar [server-address] [port]
```

The default server address is localhost and the default port is 5000 if not specified.

## Screenshots

Screenshots of the game can be found in the `src/main/resources/screenshots` directory.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributors

- Omer Yasir Onal

## Acknowledgments

- Java Swing for the GUI components
- Java Socket API for networking functionality 