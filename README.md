# Network Chess Game

A multiplayer chess game implemented in Java with client-server architecture, allowing two players to play chess over a network connection.

## Features

- Full implementation of standard chess rules
- Network multiplayer functionality
- Simple and intuitive GUI
- In-game chat between players
- Move validation and game state tracking

## Technical Architecture

The project is built using Java and follows a client-server architecture:

- **Server**: Manages game logic, validates moves, and coordinates communication between players
- **Client**: Provides the graphical interface and handles user interaction
- **Communication**: Uses socket-based networking for real-time gameplay

## Requirements

- Java Development Kit (JDK) 11 or higher
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
java -jar target/chess-server.jar [port]
```

The default port is 5000 if not specified.

### Starting the Client

```bash
java -jar target/chess-client.jar [server-address] [port]
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