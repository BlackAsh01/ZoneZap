#!/bin/bash
# Start Firebase Emulators with Data Persistence
# This script imports existing data and exports on exit

echo "Starting Firebase Emulators with data persistence..."
echo "Data will be imported from: .firebase/emulator-export"
echo "Data will be exported on exit to: .firebase/emulator-export"
echo ""

# Create export directory if it doesn't exist
mkdir -p .firebase/emulator-export

# Start emulators with import and export
firebase emulators:start --import=.firebase/emulator-export --export-on-exit=.firebase/emulator-export
