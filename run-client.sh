#!/bin/bash

# Shell script to compile and run the Chess Client

echo "Building Chess Client..."
mvn clean package -DskipTests

echo "Starting Chess Client..."
echo "Server: 141.147.25.123:9999"
java -jar target/chess-client-1.4.0.jar 