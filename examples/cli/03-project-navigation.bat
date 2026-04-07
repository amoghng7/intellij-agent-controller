@echo off
REM Example 3: Project Navigation
REM Demonstrates navigating within a project structure
REM
REM What it does:
REM 1. Get current project information
REM 2. Navigate using IDE commands
REM 3. Open files in different locations
REM 4. Take diagnostic screenshots
REM
REM Usage: 03-project-navigation.bat
REM
REM Prerequisites:
REM - IntelliJ IDEA with a project open
REM - Robot Server plugin installed
REM - JAR built: gradlew shadowJar

setlocal enabledelayedexpansion

if "%JAR_PATH%"=="" (
    set "JAR_PATH=..\..\cli\build\libs\intellij-agent-controller.jar"
)

if not exist "%JAR_PATH%" (
    echo Error: JAR not found at %JAR_PATH%
    exit /b 1
)

echo === Project Navigation Example ===
echo.

REM Step 1: Get project information
echo [1/5] Getting project information...
echo Project path:
java -jar "%JAR_PATH%" get-project-path
echo.
echo ✓ Project info retrieved
echo.

REM Step 2: Take initial screenshot
echo [2/5] Taking initial screenshot...
java -jar "%JAR_PATH%" screenshot --output nav_initial.png
if !errorlevel! neq 0 goto :error
echo ✓ Initial state captured
echo.

REM Step 3: Use Go to File navigation
echo [3/5] Navigating to a file using GoToFile...
java -jar "%JAR_PATH%" navigate "Main.kt" --type file
if !errorlevel! neq 0 goto :error
timeout /t 2 /nobreak > nul
echo ✓ Navigated to file
echo.

REM Step 4: Use Go to Class navigation
echo [4/5] Navigating to a class...
java -jar "%JAR_PATH%" navigate "ArrayList" --type class
if !errorlevel! neq 0 goto :error
timeout /t 2 /nobreak > nul
echo ✓ Navigated to class
echo.

REM Step 5: Take final screenshot
echo [5/5] Taking final screenshot...
java -jar "%JAR_PATH%" screenshot --output nav_final.png
if !errorlevel! neq 0 goto :error
echo ✓ Final state captured
echo.

echo === Complete ===
echo Initial: nav_initial.png
echo Final:   nav_final.png
echo.
echo Note: Navigation behavior depends on IDE state and open projects
exit /b 0

:error
echo.
echo Error occurred during execution
exit /b 1
