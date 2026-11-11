#!/usr/bin/env bash
# ==============================================================================
# Smart Lost & Found - Standalone Localhost Web Server Launcher
# Runs the Web Feed Server on http://localhost:8080/ in terminal / background mode.
# ==============================================================================
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

if ! command -v java &>/dev/null; then
    echo "Error: Java runtime not found. Please install OpenJDK 11 or later."
    exit 1
fi

echo "=========================================================="
echo "Starting Standalone Web Feed Server on http://localhost:8080/"
echo "=========================================================="

java -jar SmartLostFound.jar --server
