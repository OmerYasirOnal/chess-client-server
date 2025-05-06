#!/bin/bash

# Chess Client DMG Builder for macOS
# This script packages the Chess client into a macOS .dmg file

set -e  # Exit on error

echo "Building Chess Client DMG for macOS..."

# Check if jpackage is available
if ! command -v jpackage &> /dev/null; then
    echo "Error: jpackage not found. Please ensure you have JDK 14 or newer installed."
    exit 1
fi

# Make sure we're in the project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Ensure the JAR file exists
CLIENT_JAR="target/chess-client-1.5.0.jar"
if [ ! -f "$CLIENT_JAR" ]; then
    echo "Error: Client JAR file not found at $CLIENT_JAR"
    echo "Building the client JAR first..."
    mvn clean package
    if [ ! -f "$CLIENT_JAR" ]; then
        echo "Failed to build the client JAR file. Exiting."
        exit 1
    fi
fi

# Create output directory if it doesn't exist
mkdir -p release/macos

# Ensure the icon exists
ICON_FILE="src/main/resources/icons/chess-icon.icns"
if [ ! -f "$ICON_FILE" ]; then
    echo "Error: Icon file not found at $ICON_FILE"
    echo "Please ensure the icon file exists before running this script."
    exit 1
fi

# Build the DMG using jpackage
jpackage \
    --input target \
    --main-jar chess-client-1.5.0.jar \
    --name "Chess Game" \
    --app-version "1.5.0" \
    --vendor "Chess Game Project" \
    --copyright "Copyright © 2023" \
    --description "Networked Chess Game Client" \
    --main-class com.chess.client.ChessClientSwing \
    --type dmg \
    --icon "$ICON_FILE" \
    --dest release/macos

echo ""
echo "DMG creation completed!"
echo "Your DMG file is available at: release/macos/Chess-Game-1.5.0.dmg" 