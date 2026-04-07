# Troubleshooting Guide

Solutions to common issues and error messages.

## Connection Issues

### Error: "Connection refused" or "Failed to connect to robot server"

**Cause:** IntelliJ IDE isn't running or the Robot Server plugin isn't accessible.

**Solutions:**

1. **Ensure IntelliJ is running:**
   ```bash
   # Check if IDE process exists
   ps aux | grep IntelliJ  # macOS/Linux
   tasklist | findstr IntelliJ  # Windows
   ```

2. **Install Robot Server plugin:**
   - Open IntelliJ
   - Go to **Settings → Plugins** (or **Preferences → Plugins** on macOS)
   - Search for "Robot Server"
   - Click **Install** if not already installed
   - Click **Restart IDE** to activate

3. **Verify connection manually:**
   ```bash
   java -jar intellij-agent-controller.jar connect --debug
   ```
   This shows detailed HTTP requests and responses.

4. **Check port availability:**
   ```bash
   # macOS/Linux
   lsof -i :8580
   
   # Windows (PowerShell)
   netstat -ano | findstr :8580
   ```
   
   If port 8580 is in use by something else, configure a different port:
   ```bash
   java -jar intellij-agent-controller.jar connect --port 8581
   ```

### Error: "Connection timeout"

**Cause:** IDE is running but not responding (indexing, heavy operation, or plugin issues).

**Solutions:**

1. **Wait for IDE to finish indexing:**
   IntelliJ performs background indexing after opening projects. Wait 30-60s and retry.

2. **Check IDE logs:**
   ```bash
   java -jar intellij-agent-controller.jar get-logs
   ```
   Look for errors or exceptions.

3. **Restart the IDE:**
   Close and reopen IntelliJ completely. Sometimes the robot-server plugin needs a restart.

4. **Increase timeout:**
   ```bash
   java -jar intellij-agent-controller.jar connect --debug
   ```
   Add `--timeout 30000` to any command expecting slow operations.

---

## Component Finding Issues

### Error: "Component not found" or "No components matching XPath"

**Cause:** XPath selector is incorrect or component doesn't exist.

**Solutions:**

1. **Take a screenshot first:**
   ```bash
   java -jar intellij-agent-controller.jar screenshot --output screen.png
   ```
   Verify the component is visible on screen.

2. **Dump component hierarchy:**
   ```bash
   java -jar intellij-agent-controller.jar get-hierarchy --format json | head -100
   ```
   Check actual component names and structure.

3. **Use correct XPath syntax:**
   - Wrong: `//Button[@text=OK]` (missing quotes)
   - Correct: `//JButton[@text='OK']` or `//div[@class='JButton']`

4. **Common XPath patterns:**
   ```xpath
   // Find by text (exact)
   //JButton[@text='OK']
   
   // Find by text (partial)
   //*[contains(@text, 'Search')]
   
   // Find by class
   //div[@className='JPanel']
   
   // Find by index
   //JButton[0]
   
   // Find nested
   //JTree//JCheckBox
   
   // Find visible only
   //*[@visible='true']
   
   // Find enabled only
   //*[@enabled='true']
   ```

5. **Test XPath with find-all:**
   ```bash
   # List all components
   java -jar intellij-agent-controller.jar find-all --json
   
   # Filter results
   java -jar intellij-agent-controller.jar find-all --xpath "//JButton"
   ```

### Error: "XPath validation failed"

**Cause:** XPath syntax is invalid.

**Cause:** XPath contains special characters that need escaping.

**Solutions:**

1. **Quote the XPath:**
   ```bash
   # Wrong
   java -jar intellij-agent-controller.jar click //JButton[@text='O'K']
   
   # Correct (single or double quotes, consistently)
   java -jar intellij-agent-controller.jar click "//JButton[@text='OK']"
   ```

2. **Escape special characters in text:**
   ```bash
   # Component contains apostrophe: "O'Reilly"
   java -jar intellij-agent-controller.jar click "//JButton[@text=\"O'Reilly\"]"
   ```

---

## Operation Failures

### Error: "Component is not visible/enabled" when trying to interact

**Cause:** Component exists but is not interactive (hidden, disabled, or grayed out).

**Solutions:**

1. **Verify visibility:**
   ```bash
   java -jar intellij-agent-controller.jar component-exists "//JButton[@text='OK']"
   java -jar intellij-agent-controller.jar get-property "//JButton[@text='OK']" "visible"
   java -jar intellij-agent-controller.jar get-property "//JButton[@text='OK']" "enabled"
   ```

2. **Wait for component to become enabled:**
   ```bash
   java -jar intellij-agent-controller.jar wait-for "//JButton[@text='OK']" --condition enabled
   ```

3. **Scroll or navigate to reveal component:**
   ```bash
   java -jar intellij-agent-controller.jar press-key "pagedown"
   # Then retry the click
   ```

4. **Close overlapping dialogs:**
   ```bash
   java -jar intellij-agent-controller.jar find-dialog
   java -jar intellij-agent-controller.jar close-dialog
   ```

### Error: "Click had no effect" or action seemed to do nothing

**Cause:** Click registered but target wasn't reactive (wrong target, already in that state, or rapid successive clicks).

**Solutions:**

1. **Add wait between operations:**
   ```bash
   java -jar intellij-agent-controller.jar click "//JButton[@text='OK']"
   # Wait for IDE to process
   java -jar intellij-agent-controller.jar smart-wait --timeout 5000
   # Then verify state changed
   java -jar intellij-agent-controller.jar screenshot
   ```

2. **Use double-click for some UI elements:**
   ```bash
   java -jar intellij-agent-controller.jar double-click "//ItemsTable//TableCell[0]"
   ```

3. **Try alternative XPath (exact vs. partial):**
   - Wrong: `//button` (might match unintended target)
   - Correct: `//JButton[@text='Save']` (more specific)

---

## IDE Response Problems

### Error: "IDE became unresponsive" or "Timeout during operation"

**Cause:** IDE is performing a heavy operation (build, long-running test, indexing).

**Solutions:**

1. **Wait longer:**
   ```bash
   java -jar intellij-agent-controller.jar smart-wait --timeout 120000
   ```

2. **Take a screenshot to see state:**
   ```bash
   java -jar intellij-agent-controller.jar screenshot --output current.png
   ```
   Look for progress bars, build status, or indexing indicators.

3. **Check if IDE is building/indexing:**
   ```bash
   # This waits for indexing to complete
   java -jar intellij-agent-controller.jar smart-wait --timeout 60000
   ```

4. **Gracefully shut down and restart:**
   ```bash
   java -jar intellij-agent-controller.jar shutdown
   # Wait 10 seconds
   java -jar intellij-agent-controller.jar launch --ide IC --version 2024.1
   ```

### Error: "IDE crashed" or process terminated

**Cause:** IntelliJ crashed (out of memory, plugin conflict, IDE bug).

**Solutions:**

1. **Check logs:**
   ```bash
   # Get IDE logs if still running
   java -jar intellij-agent-controller.jar get-logs
   ```

2. **Check system resources:**
   ```bash
   # Memory usage
   df -h  # Disk space
   free -h  # Memory (Linux)
   ```

3. **Restart with more memory:**
   ```bash
   export JAVA_OPTS="-Xmx4g"
   java -jar intellij-agent-controller.jar launch --ide IC
   ```

4. **Disable plugins if conflict suspected:**
   - Open IntelliJ manually
   - Go to Settings → Plugins
   - Disable suspicious plugins
   - Restart IDE

---

## File & Editor Issues

### Error: "File not found" when opening a file

**Cause:** File path is incorrect or file doesn't exist.

**Solutions:**

1. **Use correct path format:**
   ```bash
   # Relative to project root
   java -jar intellij-agent-controller.jar open-file "src/main/kotlin/Main.kt"
   
   # Absolute path
   java -jar intellij-agent-controller.jar open-file "/Users/you/project/src/Main.kt"
   ```

2. **Verify file exists:**
   ```bash
   ls "src/main/kotlin/Main.kt"  # macOS/Linux
   dir "src\main\kotlin\Main.kt"  # Windows
   ```

3. **Get current project path first:**
   ```bash
   java -jar intellij-agent-controller.jar get-project-path
   ```
   Use this to construct correct file paths.

### Error: "No editor is open" when trying to read editor content

**Cause:** No file is currently open in the editor.

**Solutions:**

1. **Open a file first:**
   ```bash
   java -jar intellij-agent-controller.jar open-file "src/Main.kt"
   java -jar intellij-agent-controller.jar get-editor-text
   ```

2. **Take screenshot to see current state:**
   ```bash
   java -jar intellij-agent-controller.jar screenshot
   ```

---

## MCP & Integration Issues

### Error: "Tool not found" in Claude/Cursor

**Cause:** MCP server isn't connected or config is incorrect.

**Solutions:**

1. **Verify MCP config file exists and is valid JSON:**
   ```bash
   # macOS/Linux
   cat ~/.claude/claude_desktop_config.json
   
   # Windows
   Get-Content $env:APPDATA\Claude\claude_desktop_config.json
   ```

2. **Check JAR path is correct:**
   ```bash
   ls /full/path/to/intellij-agent-controller.jar
   # File should exist and be readable
   ```

3. **Test MCP manually:**
   ```bash
   java -jar intellij-agent-controller.jar mcp-server --transport stdio < /dev/null
   # Should start and exit cleanly
   ```

4. **Restart Claude/Cursor:**
   - Close completely (not just minimize)
   - Wait 5 seconds
   - Reopen

5. **Check Claude logs:**
   ```bash
   # macOS/Linux
   tail -100 ~/.claude/logs/mcp-server.log
   ```

### Error: "MCP server timed out"

**Cause:** MCP server took too long to respond or IDE was slow.

**Solutions:**

1. **Increase system resources:**
   IDE operations need CPU and RAM. Close other apps.

2. **Use smaller operations:**
   - Instead of `find-all` on massive IDE, use targeted `find` with specific XPath
   - Take smaller screenshots (use `--component` to capture just part of screen)

3. **Check IDE state:**
   ```bash
   java -jar intellij-agent-controller.jar screenshot
   java -jar intellij-agent-controller.jar status
   ```

---

## Performance Issues

### "Operations are very slow" (5-30 seconds each)

**Causes & Solutions:**

1. **IDE indexing:**
   - Wait for indexing: `java -jar intellij-agent-controller.jar smart-wait --timeout 60000`
   - Check status: `java -jar intellij-agent-controller.jar status`

2. **Large project:**
   - Many components slow down `find-all` and screenshots
   - Use specific XPath: `//JButton[@text='OK']` instead of `//*`

3. **Network latency (remote IDE):**
   - Use SSH tunnel for better latency
   - Use SSE transport instead of stdio for MCP

4. **Heavy IDE operations:**
   - Build in progress
   - Tests running
   - Debugger active
   - Wait for completion before continuing

5. **Debug mode overhead:**
   - Remove `--debug` flag from production commands
   - It logs every HTTP request (slows things down)

### Tool runs fine in CLI but slow in MCP

**Cause:** Stdio transport has overhead. MCP is for convenience, not maximum performance.

**Solutions:**

1. **Use SSE transport for better performance:**
   ```bash
   java -jar intellij-agent-controller.jar mcp-server --transport sse --mcp-port 3000
   ```

2. **Use CLI directly for complex workflows:**
   ```bash
   # Faster than MCP for batch operations
   ./gradlew shadowJar
   java -jar cli/build/libs/intellij-agent-controller.jar find-all
   ```

---

## Debug Mode

### Enable detailed logging

```bash
java -jar intellij-agent-controller.jar --debug <COMMAND>
```

This shows:
- HTTP requests and responses
- Timings for each operation
- Error stack traces

Example:

```bash
java -jar intellij-agent-controller.jar --debug click "//JButton[@text='OK']"

# Output:
# [DEBUG] POST http://127.0.0.1:8580/api/interaction/click
# [DEBUG] Request: {"xpath": "//JButton[@text='OK']"}
# [DEBUG] Response time: 123ms
# [DEBUG] Response: {"success": true}
# ✓ Clicked component
```

---

## Getting Help

If issues persist:

1. **Collect diagnostic info:**
   ```bash
   java -jar intellij-agent-controller.jar screenshot --output debug.png
   java -jar intellij-agent-controller.jar get-hierarchy --format json > hierarchy.json
   java -jar intellij-agent-controller.jar --debug status > debug.log 2>&1
   ```

2. **Check system info:**
   - Java version: `java -version`
   - IDE version: See IntelliJ Help → About
   - OS: `uname -a` (macOS/Linux) or `ver` (Windows)

3. **File an issue:**
   - Include debug.log, screenshot, and hierarchy.json
   - Describe what you were trying to do
   - Include exact error message

---

## Common Error Messages

| Message | Cause | Solution |
|---------|-------|----------|
| `Connection refused` | IDE not running or port blocked | Start IDE, check port 8580 |
| `XPath validation failed` | Invalid XPath syntax | Quote the XPath, check syntax |
| `Component not found` | Component doesn't exist or wrong XPath | Take screenshot, check hierarchy |
| `Timeout` | Operation took too long | Wait for IDE, increase timeout |
| `No editor is open` | Trying to read editor without open file | Open a file first |
| `File not found` | File doesn't exist at path | Verify path, use absolute or relative correctly |
| `Component not visible` | Component exists but hidden/disabled | Wait, scroll, or close dialogs |
| `IDE became unresponsive` | IDE is busy or crashed | Wait, check logs, restart IDE |

→ **[Getting Started](GETTING_STARTED.md)**  
→ **[CLI Reference](CLI_REFERENCE.md)**
