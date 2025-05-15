#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Building Chess Server...${NC}"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed! Cannot start server.${NC}"
    exit 1
fi

echo -e "${GREEN}Starting Chess Server...${NC}"
java -jar target/chess-server-1.5.1.jar 