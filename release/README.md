# Chess Game Release Files

## Version 1.5.1 (May 15, 2025)

This release includes important improvements to connection handling, client disconnection detection, and username management.

### What's New

- Improved connection management system
  - Added client-server heartbeat mechanism with ping/pong messages
  - Implemented automatic detection and cleanup of disconnected clients
  - Fixed issues with username availability after player disconnections
  - Added socket timeouts for faster connection state detection
  - Implemented periodic client checker for stale connections
- Enhanced player experience during opponent disconnections
- Optimized server resources through better client lifecycle management
- Improved server stability and reliability

### Files Included

- `jar/chess-client-1.5.1.jar` - Client application (requires Java 17+)
- `jar/chess-server-1.5.1.jar` - Server application (requires Java 17+)
- `macos/Chess Game-1.5.1.dmg` - macOS installer package

### Installation

#### macOS Users
1. Download the `Chess Game-1.5.1.dmg` file
2. Double-click the DMG file to open it
3. Drag the Chess Game icon to the Applications folder
4. Open the application from your Applications folder or Launchpad

#### Other Platforms
1. Ensure you have Java 17 or higher installed
2. Download the chess-client-1.5.1.jar file
3. Run the client using the following command:
   ```
   java -jar chess-client-1.5.1.jar
   ```

### Server Deployment

For server administrators:
1. Ensure you have Java 17 or higher installed
2. Download the chess-server-1.5.1.jar file
3. Run the server using the following command:
   ```
   java -jar chess-server-1.5.1.jar
   ```
4. The server runs on port 9999 by default

## Previous Versions

### Version 1.5.0 (May 5, 2025)

- Removed time control mechanism
- Implemented auto-start game feature
- Added macOS DMG installer with pawn icon
- UI improvements and bug fixes 