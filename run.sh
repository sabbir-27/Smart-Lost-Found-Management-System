#!/usr/bin/env bash
# ==============================================================================
# Smart Lost & Found - Desktop Application & Localhost Web Server Launcher
# Launches the Executive Desktop UI and starts the localhost:8080 Web Feed Server.
# ==============================================================================
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

# Load environment configuration from .env if present
if [ -f .env ]; then
    set -a
    source .env 2>/dev/null || true
    set +a
fi

if ! command -v java &>/dev/null; then
    echo "Error: Java runtime not found. Please install OpenJDK 11 or later."
    exit 1
fi

echo "=========================================================="
echo "Launching Smart Lost & Found Management System"
echo "Web Feed Server: http://localhost:8080/"
echo "=========================================================="

java -jar SmartLostFound.jar "$@"
