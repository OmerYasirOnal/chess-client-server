#!/bin/bash

echo "Building Chess Server..."
mvn clean package -DskipTests

echo "Starting Chess Server on localhost..."
java -jar target/chess-server-1.4.0.jar 