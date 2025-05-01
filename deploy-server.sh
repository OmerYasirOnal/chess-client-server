#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Server details
SERVER_IP="141.147.25.123"
SSH_KEY="~/.ssh/id_ed25519"
SERVER_USER="ubuntu"
SERVER_DIR="~/chess-server"
SERVER_NAME="chess-server"

echo -e "${GREEN}Starting chess server deployment...${NC}"

# 1. Build the JAR locally
echo -e "${GREEN}Building server JAR...${NC}"
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed! Aborting deployment.${NC}"
    exit 1
fi

# Check that jar file exists
JAR_FILE="target/chess-server-1.5.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}JAR file not found after build! Aborting deployment.${NC}"
    exit 1
fi

# 2. Connect to the server and stop the current instance
echo -e "${GREEN}Stopping the current server instance...${NC}"
ssh -i "$SSH_KEY" "$SERVER_USER@$SERVER_IP" "cd $SERVER_DIR && pm2 stop $SERVER_NAME || true"

# 3. Upload the new JAR file
echo -e "${GREEN}Uploading new JAR file...${NC}"
scp -i "$SSH_KEY" "$JAR_FILE" "$SERVER_USER@$SERVER_IP:$SERVER_DIR/chess-server.jar"
if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to upload JAR file! Aborting deployment.${NC}"
    exit 1
fi

# 4. Start the server using PM2
echo -e "${GREEN}Starting the server...${NC}"
ssh -i "$SSH_KEY" "$SERVER_USER@$SERVER_IP" "cd $SERVER_DIR && pm2 start chess-server.jar --name=$SERVER_NAME || pm2 restart $SERVER_NAME"

# 5. Check server status
echo -e "${GREEN}Checking server status...${NC}"
ssh -i "$SSH_KEY" "$SERVER_USER@$SERVER_IP" "cd $SERVER_DIR && pm2 status"

echo -e "${GREEN}Deployment completed successfully!${NC}" 