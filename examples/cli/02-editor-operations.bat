@echo off
REM Example 2: Editor Operations
REM Demonstrates opening files, reading content, and executing editor actions
REM
REM What it does:
REM 1. Get current project path
REM 2. Open a specific file (src/Main.kt)
REM 3. Read the editor content
REM 4. Execute ReformatCode action
REM 5. Take screenshots at key points
REM
REM Usage: 02-editor-operations.bat [FILE_PATH]
REM Example: 02-editor-operations.bat "src\main\kotlin\Main.kt"
REM
REM Prerequisites:
REM - IntelliJ IDEA is running with a project open
REM - Robot Server plugin is installed
REM - JAR is built: gradlew shadowJar

setlocal enabledelayedexpansion

REM Configuration
if "%JAR_PATH%"=="" (
    set "JAR_PATH=..\..\cli\build\libs\intellij-agent-controller.jar"
)

if "%1"=="" (
    set "FILE_PATH=src\Main.kt"
) else (
    set "FILE_PATH=%1"
)

if not exist "%JAR_PATH%" (
    echo Error: JAR not found at %JAR_PATH%
    exit /b 1
)

echo === Editor Operations Example ===
echo.

REM Step 1: Check project path
echo [1/5] Getting current project path...
for /f "delims=" %%i in ('java -jar "%JAR_PATH%" get-project-path') do set "PROJECT_PATH=%%i"
echo ✓ Project: !PROJECT_PATH!
echo.

REM Step 2: Take screenshot of current state
echo [2/5] Taking screenshot before file open...
java -jar "%JAR_PATH%" screenshot --output editor_before.png
if !errorlevel! neq 0 goto :error
echo ✓ Screenshot saved: editor_before.png
echo.

REM Step 3: Open file
echo [3/5] Opening file: %FILE_PATH%
java -jar "%JAR_PATH%" open-file "%FILE_PATH%"
if !errorlevel! neq 0 goto :error
echo ✓ File opened
timeout /t 2 /nobreak > nul
echo.

REM Step 4: Get editor info
echo [4/5] Reading editor information...
java -jar "%JAR_PATH%" editor-info
echo.

REM Step 5: Execute format action
echo [5/5] Executing ReformatCode action...
java -jar "%JAR_PATH%" editor-action "ReformatCode"
if !errorlevel! neq 0 goto :error
echo ✓ Code formatted
timeout /t 1 /nobreak > nul
echo.

REM Final: Take screenshot after format
java -jar "%JAR_PATH%" screenshot --output editor_after.png
if !errorlevel! neq 0 goto :error
echo ✓ Screenshot saved: editor_after.png
echo.

echo === Complete ===
echo File: %FILE_PATH%
echo Before: editor_before.png
echo After:  editor_after.png
exit /b 0

:error
echo.
echo Error occurred during execution
exit /b 1
