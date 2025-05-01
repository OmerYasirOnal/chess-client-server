#!/bin/bash

echo "Building Chess Client..."
mvn clean package -DskipTests

echo "Starting Chess Client (localhost)..."
java -jar target/chess-client-1.4.0.jar 