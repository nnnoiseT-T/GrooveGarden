#!/bin/bash

# Simple compilation script for Groove Garden
echo "Compiling Groove Garden..."

# Create output directory
mkdir -p build/classes

# Compile all Java files
javac -d build/classes \
    -cp ".:build/classes" \
    src/main/java/com/groovegarden/*.java \
    src/main/java/com/groovegarden/ui/*.java \
    src/main/java/com/groovegarden/model/*.java \
    src/main/java/com/groovegarden/algo/*.java \
    src/main/java/com/groovegarden/music/*.java \
    src/main/java/com/groovegarden/score/*.java \
    src/main/java/com/groovegarden/util/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "You can now run the application with:"
    echo "java -cp build/classes com.groovegarden.MainApp"
else
    echo "Compilation failed!"
    exit 1
fi 