#!/bin/bash

# Example 4: Actions and Dialogs
# Demonstrates invoking IDE actions and handling dialogs
#
# What it does:
# 1. Take initial screenshot
# 2. Invoke an IDE action (Build)
# 3. Wait for IDE to respond
# 4. Handle any dialogs that appear
# 5. Take final screenshot showing results
#
# Usage: ./04-actions-and-dialogs.sh [ACTION]
# Example: ./04-actions-and-dialogs.sh "Build"
#
# Prerequisites:
# - IntelliJ IDEA with a project open
# - Robot Server plugin installed
# - JAR built: ./gradlew shadowJar

set -e

JAR_PATH="${JAR_PATH:-../../cli/build/libs/intellij-agent-controller.jar}"
ACTION="${1:-Build}"

if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH"
    exit 1
fi

echo "=== Actions & Dialogs Example ==="
echo "Action: $ACTION"
echo ""

# Step 1: Take screenshot before action
echo "[1/5] Taking screenshot before action..."
java -jar "$JAR_PATH" screenshot --output action_before.png
echo "✓ Before screenshot saved"
echo ""

# Step 2: Invoke the action
echo "[2/5] Invoking action: $ACTION"
java -jar "$JAR_PATH" invoke-action "$ACTION"
echo "✓ Action invoked"
sleep 2
echo ""

# Step 3: Wait for IDE to process
echo "[3/5] Waiting for IDE to respond..."
java -jar "$JAR_PATH" smart-wait --timeout 30000
echo "✓ IDE ready"
echo ""

# Step 4: Check for dialogs
echo "[4/5] Checking for dialogs..."
if java -jar "$JAR_PATH" find-dialog > /dev/null 2>&1; then
    echo "⚠ Dialog detected, closing it..."
    java -jar "$JAR_PATH" close-dialog --action "ok"
    echo "✓ Dialog closed"
else
    echo "✓ No dialogs found"
fi
echo ""

# Step 5: Take final screenshot
echo "[5/5] Taking screenshot after action..."
java -jar "$JAR_PATH" screenshot --output action_after.png
echo "✓ After screenshot saved"
echo ""

echo "=== Complete ==="
echo "Before: action_before.png"
echo "After:  action_after.png"
echo ""
echo "Compare screenshots to see the result of the action"
