#!/bin/bash

# Example 3: Project Navigation
# Demonstrates navigating within a project structure
#
# What it does:
# 1. Get current project information
# 2. Navigate using IDE commands
# 3. Open files in different locations
# 4. Take diagnostic screenshots
#
# Usage: ./03-project-navigation.sh
#
# Prerequisites:
# - IntelliJ IDEA with a project open
# - Robot Server plugin installed
# - JAR built: ./gradlew shadowJar

set -e

JAR_PATH="${JAR_PATH:-../../cli/build/libs/intellij-agent-controller.jar}"

if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH"
    exit 1
fi

echo "=== Project Navigation Example ==="
echo ""

# Step 1: Get project information
echo "[1/5] Getting project information..."
echo "Project path:"
java -jar "$JAR_PATH" get-project-path
echo ""
echo "✓ Project info retrieved"
echo ""

# Step 2: Take initial screenshot
echo "[2/5] Taking initial screenshot..."
java -jar "$JAR_PATH" screenshot --output nav_initial.png
echo "✓ Initial state captured"
echo ""

# Step 3: Use Go to File navigation
echo "[3/5] Navigating to a file using GoToFile..."
java -jar "$JAR_PATH" navigate "Main.kt" --type file
sleep 2
echo "✓ Navigated to file"
echo ""

# Step 4: Use Go to Class navigation
echo "[4/5] Navigating to a class..."
java -jar "$JAR_PATH" navigate "ArrayList" --type class
sleep 2
echo "✓ Navigated to class"
echo ""

# Step 5: Take final screenshot
echo "[5/5] Taking final screenshot..."
java -jar "$JAR_PATH" screenshot --output nav_final.png
echo "✓ Final state captured"
echo ""

echo "=== Complete ==="
echo "Initial: nav_initial.png"
echo "Final:   nav_final.png"
echo ""
echo "Note: Navigation behavior depends on IDE state and open projects"
