#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Default server details
DEFAULT_SERVER="141.147.25.123"
DEFAULT_PORT="9999"

# Check if a server argument was provided
if [ -n "$1" ]; then
    SERVER="$1"
else
    SERVER=$DEFAULT_SERVER
fi

echo -e "${GREEN}Building Chess Client...${NC}"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed! Cannot start client.${NC}"
    exit 1
fi

echo -e "${GREEN}Starting Chess Client...${NC}"
echo -e "${GREEN}Server: $SERVER:$DEFAULT_PORT${NC}"
java -jar target/chess-client-1.5.0.jar 