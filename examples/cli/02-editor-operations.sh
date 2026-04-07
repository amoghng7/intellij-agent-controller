#!/bin/bash

# Example 2: Editor Operations
# Demonstrates opening files, reading content, and executing editor actions
#
# What it does:
# 1. Get current project path
# 2. Open a specific file (src/Main.kt)
# 3. Read the editor content
# 4. Execute ReformatCode action
# 5. Take screenshots at key points
#
# Usage: ./02-editor-operations.sh [FILE_PATH]
# Example: ./02-editor-operations.sh "src/main/kotlin/Main.kt"
#
# Prerequisites:
# - IntelliJ IDEA is running with a project open
# - Robot Server plugin is installed
# - JAR is built: ./gradlew shadowJar

set -e

# Configuration
JAR_PATH="${JAR_PATH:-../../cli/build/libs/intellij-agent-controller.jar}"
FILE_PATH="${1:-src/Main.kt}"

if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH"
    exit 1
fi

echo "=== Editor Operations Example ==="
echo ""

# Step 1: Check project path
echo "[1/5] Getting current project path..."
PROJECT_PATH=$(java -jar "$JAR_PATH" get-project-path)
echo "✓ Project: $PROJECT_PATH"
echo ""

# Step 2: Take screenshot of current state
echo "[2/5] Taking screenshot before file open..."
java -jar "$JAR_PATH" screenshot --output editor_before.png
echo "✓ Screenshot saved: editor_before.png"
echo ""

# Step 3: Open file
echo "[3/5] Opening file: $FILE_PATH"
java -jar "$JAR_PATH" open-file "$FILE_PATH"
echo "✓ File opened"
sleep 2  # Wait for file to fully load
echo ""

# Step 4: Get editor info
echo "[4/5] Reading editor information..."
java -jar "$JAR_PATH" editor-info
echo ""

# Step 5: Execute format action
echo "[5/5] Executing ReformatCode action..."
java -jar "$JAR_PATH" editor-action "ReformatCode"
echo "✓ Code formatted"
sleep 1
echo ""

# Final: Take screenshot after format
java -jar "$JAR_PATH" screenshot --output editor_after.png
echo "✓ Screenshot saved: editor_after.png"
echo ""

echo "=== Complete ==="
echo "File: $FILE_PATH"
echo "Before: editor_before.png"
echo "After:  editor_after.png"
