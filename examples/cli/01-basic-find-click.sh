#!/bin/bash

# Example 1: Basic Find & Click
# Demonstrates finding a UI component and clicking it
#
# What it does:
# 1. Take a screenshot to see the current IDE state
# 2. Find the "Run" button using XPath
# 3. Click the button
# 4. Take another screenshot to verify the click
#
# Usage: ./01-basic-find-click.sh
# 
# Prerequisites:
# - IntelliJ IDEA is running
# - Robot Server plugin is installed
# - JAR is built: ./gradlew shadowJar

set -e  # Exit on error

# Path to the JAR file
JAR_PATH="${JAR_PATH:-../../cli/build/libs/intellij-agent-controller.jar}"

# Verify JAR exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH"
    echo "Build it with: ./gradlew shadowJar"
    exit 1
fi

echo "=== Basic Find & Click Example ==="
echo ""

# Step 1: Connect and take initial screenshot
echo "[1/4] Taking screenshot of current IDE state..."
java -jar "$JAR_PATH" screenshot --output before_click.png
echo "✓ Screenshot saved: before_click.png"
echo ""

# Step 2: Find the Run button
echo "[2/4] Finding the Run button..."
java -jar "$JAR_PATH" find "//div[@class='JButton' and @text='Run']" > /dev/null
echo "✓ Run button found"
echo ""

# Step 3: Click the button
echo "[3/4] Clicking the Run button..."
java -jar "$JAR_PATH" click "//div[@class='JButton' and @text='Run']"
echo "✓ Button clicked"
echo ""

# Step 4: Wait a moment for IDE to respond, then take screenshot
echo "[4/4] Taking screenshot after click..."
sleep 1
java -jar "$JAR_PATH" screenshot --output after_click.png
echo "✓ Screenshot saved: after_click.png"
echo ""

echo "=== Complete ==="
echo "Before screenshot: before_click.png"
echo "After screenshot:  after_click.png"
echo ""
echo "Compare the screenshots to verify the button click took effect."
