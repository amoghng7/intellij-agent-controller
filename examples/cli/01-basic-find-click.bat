@echo off
REM Example 1: Basic Find & Click
REM Demonstrates finding a UI component and clicking it
REM
REM What it does:
REM 1. Take a screenshot to see the current IDE state
REM 2. Find the "Run" button using XPath
REM 3. Click the button
REM 4. Take another screenshot to verify the click
REM
REM Usage: 01-basic-find-click.bat
REM 
REM Prerequisites:
REM - IntelliJ IDEA is running
REM - Robot Server plugin is installed
REM - JAR is built: gradlew shadowJar

setlocal enabledelayedexpansion

REM Path to the JAR file
if "%JAR_PATH%"=="" (
    set "JAR_PATH=..\..\cli\build\libs\intellij-agent-controller.jar"
)

REM Verify JAR exists
if not exist "%JAR_PATH%" (
    echo Error: JAR not found at %JAR_PATH%
    echo Build it with: gradlew shadowJar
    exit /b 1
)

echo === Basic Find ^& Click Example ===
echo.

REM Step 1: Connect and take initial screenshot
echo [1/4] Taking screenshot of current IDE state...
java -jar "%JAR_PATH%" screenshot --output before_click.png
if !errorlevel! neq 0 goto :error
echo ✓ Screenshot saved: before_click.png
echo.

REM Step 2: Find the Run button
echo [2/4] Finding the Run button...
java -jar "%JAR_PATH%" find "//div[@class='JButton' and @text='Run']" > nul
if !errorlevel! neq 0 goto :error
echo ✓ Run button found
echo.

REM Step 3: Click the button
echo [3/4] Clicking the Run button...
java -jar "%JAR_PATH%" click "//div[@class='JButton' and @text='Run']"
if !errorlevel! neq 0 goto :error
echo ✓ Button clicked
echo.

REM Step 4: Wait a moment for IDE to respond, then take screenshot
echo [4/4] Taking screenshot after click...
timeout /t 1 /nobreak > nul
java -jar "%JAR_PATH%" screenshot --output after_click.png
if !errorlevel! neq 0 goto :error
echo ✓ Screenshot saved: after_click.png
echo.

echo === Complete ===
echo Before screenshot: before_click.png
echo After screenshot:  after_click.png
echo.
echo Compare the screenshots to verify the button click took effect.
exit /b 0

:error
echo.
echo Error occurred during execution
exit /b 1
