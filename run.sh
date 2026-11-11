#!/bin/bash
set -e

# Build if jar does not exist
if [ ! -f "SmartLostFound.jar" ]; then
    echo "SmartLostFound.jar not found. Running build..."
    ./build.sh
fi

echo "Launching Smart Lost & Found Management System..."
java -jar SmartLostFound.jar "$@"
