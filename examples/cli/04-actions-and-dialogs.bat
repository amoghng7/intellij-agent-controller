@echo off
REM Example 4: Actions and Dialogs
REM Demonstrates invoking IDE actions and handling dialogs
REM
REM What it does:
REM 1. Take initial screenshot
REM 2. Invoke an IDE action (Build)
REM 3. Wait for IDE to respond
REM 4. Handle any dialogs that appear
REM 5. Take final screenshot showing results
REM
REM Usage: 04-actions-and-dialogs.bat [ACTION]
REM Example: 04-actions-and-dialogs.bat "Build"
REM
REM Prerequisites:
REM - IntelliJ IDEA with a project open
REM - Robot Server plugin installed
REM - JAR built: gradlew shadowJar

setlocal enabledelayedexpansion

if "%JAR_PATH%"=="" (
    set "JAR_PATH=..\..\cli\build\libs\intellij-agent-controller.jar"
)

if "%1"=="" (
    set "ACTION=Build"
) else (
    set "ACTION=%1"
)

if not exist "%JAR_PATH%" (
    echo Error: JAR not found at %JAR_PATH%
    exit /b 1
)

echo === Actions ^& Dialogs Example ===
echo Action: %ACTION%
echo.

REM Step 1: Take screenshot before action
echo [1/5] Taking screenshot before action...
java -jar "%JAR_PATH%" screenshot --output action_before.png
if !errorlevel! neq 0 goto :error
echo ✓ Before screenshot saved
echo.

REM Step 2: Invoke the action
echo [2/5] Invoking action: %ACTION%
java -jar "%JAR_PATH%" invoke-action "%ACTION%"
if !errorlevel! neq 0 goto :error
echo ✓ Action invoked
timeout /t 2 /nobreak > nul
echo.

REM Step 3: Wait for IDE to process
echo [3/5] Waiting for IDE to respond...
java -jar "%JAR_PATH%" smart-wait --timeout 30000
echo ✓ IDE ready
echo.

REM Step 4: Check for dialogs
echo [4/5] Checking for dialogs...
java -jar "%JAR_PATH%" find-dialog > nul 2>&1
if !errorlevel! equ 0 (
    echo ⚠ Dialog detected, closing it...
    java -jar "%JAR_PATH%" close-dialog --action "ok"
    echo ✓ Dialog closed
) else (
    echo ✓ No dialogs found
)
echo.

REM Step 5: Take final screenshot
echo [5/5] Taking screenshot after action...
java -jar "%JAR_PATH%" screenshot --output action_after.png
if !errorlevel! neq 0 goto :error
echo ✓ After screenshot saved
echo.

echo === Complete ===
echo Before: action_before.png
echo After:  action_after.png
echo.
echo Compare screenshots to see the result of the action
exit /b 0

:error
echo.
echo Error occurred during execution
exit /b 1
