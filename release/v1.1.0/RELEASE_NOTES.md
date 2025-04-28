# Chess Game v1.1.0 Release Notes

This is the first update after the first stable version of our chess game project, 1.0.0.

## New Features and Improvements

* **Code Cleanup**: General cleanup and refactoring of all code
* **User Interface Improvements**: Edits were made to the UI
* **Performance Improvements**: Game engine and networking were sped up
* **Documentation Updates**: README and ChangeLog files were updated with up-to-date information

## Installation Instructions

### Server
``` bash
java -jar chess-server-1.1.0.jar [port]
''''
Default port is 5000 if not specified.

### Client
``` bash
java -jar chess-client-1.1.0.jar [server address] [port]
''''
The default server address is localhost and the default port is 5000.

## Future Release Plans

The following features are planned to be integrated in version 1.2.0:
- Name of captured pieces
- Drag-and-drop footwork
- Transfer and display of move history
- Board size and coordinate label ratios
- Chess clock plugin

For detailed information see ChangeLog.txt.