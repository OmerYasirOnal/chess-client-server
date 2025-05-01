# Chess Game User Guide

This guide will help you get started with the Chess Game, explaining how to run the game with two clients and play a chess match.

## Setup

### Prerequisites
- Make sure you have Java 17 or higher installed
- Ensure all scripts have execute permissions:
  ```bash
  chmod +x run-server.sh run-client.sh run-server-localhost.sh run-client-localhost.sh run-player1.sh run-player2.sh
  ```

## Playing on the Same Computer (Local Mode)

If you want to play with two clients on the same computer:

### Step 1: Start the Server
1. Open a terminal and navigate to the project directory
2. Run the server with:
   ```bash
   ./run-server-localhost.sh
   ```
3. Keep this terminal open while playing

### Step 2: Start Two Clients

#### Option 1: Using run-client-localhost.sh twice
1. Open a new terminal and run:
   ```bash
   ./run-client-localhost.sh
   ```
2. Open another terminal and run the same command:
   ```bash
   ./run-client-localhost.sh
   ```

#### Option 2: Using the player-specific scripts
1. Open a new terminal and run:
   ```bash
   ./run-player1.sh
   ```
2. Open another terminal and run:
   ```bash
   ./run-player2.sh
   ```

### Step 3: Connect Both Clients
For each client:
1. Enter a different username (e.g., "Player1" and "Player2")
2. Enter `localhost` as the server address
3. Enter `9999` as the port
4. Click "Connect"

## Playing Over the Internet

To play with someone over the internet:

### Step 1: One Player Connects to the Remote Server
1. Run the client:
   ```bash
   ./run-client.sh
   ```
2. Enter your username
3. Enter `141.147.25.123` as the server address
4. Enter `9999` as the port
5. Click "Connect"

### Step 2: Second Player Connects to the Same Server
The second player follows the same steps on their computer.

## Game Navigation

### Creating a Game
1. In the lobby, click on the "Create Game" tab
2. Click the "Create Game" button
3. Wait for an opponent to join

### Joining a Game
1. In the lobby, select a game from the list
2. Click "Join Game"
3. The game will start automatically when you join

### Playing the Game
- The board is oriented with White at the bottom and Black at the top
- White always moves first
- To move a piece:
  - Click on the piece you want to move
  - Valid move squares will be highlighted
  - Click on a highlighted square to move there
  - Alternatively, drag and drop the piece
- Use the chat panel on the right to communicate with your opponent
- The move history is shown on the right side
- Game status and whose turn it is are displayed at the top

### Game End
A game can end in several ways:
- Checkmate: When a king is in check and cannot escape
- Stalemate: When a player has no legal moves but is not in check
- Resignation: Click the "Resign" button to forfeit
- Draw offer: Click "Offer Draw" and wait for your opponent to accept
- Disconnection: If a player leaves, the remaining player wins

After the game ends, you'll return to the lobby where you can start or join a new game.

## Troubleshooting

### Connection Issues
- Make sure the server is running
- Check that you're using the correct IP address and port
- Ensure no firewall is blocking the connection

### Game Not Starting
- Both players must be connected to the server
- One player must create a game, and the other must join it

### Other Issues
- Restart the client and try connecting again
- Check the terminal for any error messages
- Restart the server if necessary

## Controls Reference

| Action | Control |
|--------|---------|
| Select piece | Left-click on a piece |
| Move piece | Left-click on a highlighted square |
| Drag piece | Click and hold on a piece, release on destination |
| Send chat | Type in chat box, press Enter or click Send |
| Resign | Click the "Resign" button |
| Offer draw | Click the "Offer Draw" button 