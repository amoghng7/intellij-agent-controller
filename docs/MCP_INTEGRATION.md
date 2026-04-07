# MCP Integration Guide

Use IntelliJ Agent Controller with AI agents via the Model Context Protocol (MCP). This guide covers Claude Desktop, Cursor, and custom MCP setups.

## What is MCP?

[Model Context Protocol](https://modelcontextprotocol.io) is a standard for connecting AI models (like Claude) to tools and data sources. IntelliJ Agent Controller exposes IDE commands as MCP tools, allowing Claude or other AI agents to control your IDE through natural language.

**Example workflow:**
1. You tell Claude: "Find and open the Main.kt file, show me the test imports"
2. Claude uses IntelliJ Agent Controller to open the file via MCP
3. Claude reads the content and responds with the test imports

---

## Claude Desktop

### Setup

1. **Build the fat JAR:**
   ```bash
   ./gradlew shadowJar
   ```

2. **Get the JAR path:**
   ```bash
   # Unix/macOS
   pwd  # Shows current directory
   # Then your JAR is at: /full/path/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar

   # Windows (PowerShell)
   (Get-Location).Path
   # Then your JAR is at: C:\Users\You\intellij-agent-controller\cli\build\libs\intellij-agent-controller.jar
   ```

3. **Edit Claude Desktop config:**

   **macOS/Linux:**
   ```bash
   nano ~/.claude/claude_desktop_config.json
   ```

   **Windows:**
   ```powershell
   notepad $env:APPDATA\Claude\claude_desktop_config.json
   ```

4. **Add the MCP server:**
   ```json
   {
     "mcpServers": {
       "intellij": {
         "command": "java",
         "args": [
           "-jar",
           "/Users/you/Projects/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar",
           "mcp-server",
           "--transport",
           "stdio"
         ]
       }
     }
   }
   ```

   Replace the path with your actual JAR location.

5. **Restart Claude Desktop:**
   - Close Claude Desktop completely
   - Reopen it
   - You should see a 🔌 MCP indicator at the bottom right (or in the model selector, depending on version)

6. **Verify it's connected:**
   - In Claude, check the "Composer Settings" or look for a MCP/Tool icon
   - You should see "intellij" listed as an available tool

### Using with Claude

Now you can ask Claude to control your IDE using natural language:

**Example prompts:**

```
Please take a screenshot of the IDE and show me what's on screen.
```

```
Open the file src/main/kotlin/Main.kt and show me the first 20 lines.
```

```
Find the "Run" button on the toolbar and click it to start the build.
```

```
Get the current project path and tell me what project is open.
```

```
Search for all files matching "*.test.kt" and list them.
```

```
Open the editor preferences and take a screenshot.
```

Claude will:
1. Break your request into IDE commands
2. Use IntelliJ Agent Controller to execute them
3. Combine results and respond to you

### Troubleshooting

**"Tool not found" or MCP icon doesn't appear:**
- Verify the JAR path in config is correct (no typos)
- Make sure the JAR was built: `ls cli/build/libs/intellij-agent-controller.jar` (or `dir` on Windows)
- Check Claude Desktop logs: `~/.claude/logs/` (macOS/Linux)

**"Connection refused" error when using Claude:**
- Ensure IntelliJ is running with Robot Server plugin installed
- Verify the IDE is on localhost:8580 (or adjust `--port` in config)
- Check firewall isn't blocking port 8580

**Slow responses:**
- MCP uses stdio, which is slower than HTTP. This is normal.
- Large screenshots or component listings may take 5-30 seconds depending on IDE size

---

## Cursor IDE

### Setup

1. **Build the JAR (same as Claude):**
   ```bash
   ./gradlew shadowJar
   ```

2. **Get the JAR path** (same as Claude setup)

3. **Configure Cursor:**
   - Open Cursor Settings → MCP
   - Click "Add MCP Tool"
   - Choose "Command"
   - Enter:
     - **Command:** `java`
     - **Args:** `-jar /path/to/intellij-agent-controller.jar mcp-server --transport stdio`

4. **Restart Cursor:**
   - Close and reopen Cursor
   - Look for IntelliJ tool availability in chat

### Using with Cursor

Same as Claude — use natural language prompts to control your IDE:

```
Help me refactor this class. First, open src/models/User.kt and show me the current code.
```

```
Run the tests for me by clicking the Run Test button and take a screenshot.
```

---

## Custom MCP Integration

For other tools or custom setups, you can run IntelliJ Agent Controller as an MCP server.

### Stdio Transport (Recommended)

Stdio is standard input/output piping. Perfect for embedding:

```bash
java -jar intellij-agent-controller.jar mcp-server --transport stdio
```

This makes IntelliJ Agent Controller read commands from stdin and write responses to stdout, compatible with any MCP client.

### SSE Transport

For HTTP-based integrations (Server-Sent Events):

```bash
java -jar intellij-agent-controller.jar mcp-server --transport sse --mcp-port 3000
```

Then connect your MCP client to `http://localhost:3000`

### Options

| Option | Default | Description |
|--------|---------|-------------|
| `--transport` | `stdio` | `stdio` or `sse` |
| `--mcp-port` | `3000` | Port for SSE transport |
| `--port` | `8580` | Robot server port |
| `--host` | `127.0.0.1` | Robot server host |

---

## Tool Reference

When connected via MCP, Claude/Cursor can use these tools:

### `take_screenshot`

Capture what's on screen.

```
Tool: take_screenshot
Parameters:
  - output_path: (optional) file to save to
  - base64: (optional) true to get base64 encoded image
```

### `find_component`

Find a UI component by XPath.

```
Tool: find_component
Parameters:
  - xpath: XPath selector (e.g., "//JButton[@text='OK']")
```

### `click_component`

Click a UI component.

```
Tool: click_component
Parameters:
  - xpath: XPath selector
```

### `open_file`

Open a file in the editor.

```
Tool: open_file
Parameters:
  - file_path: Path to file
```

### `read_editor`

Get current editor content.

```
Tool: read_editor
Returns: File content, line numbers, caret position
```

### `invoke_action`

Invoke an IDE action (Run, Build, Format, etc.).

```
Tool: invoke_action
Parameters:
  - action_id: Action ID (e.g., "Run", "Build")
```

Plus all other CLI commands exposed as MCP tools.

---

## Example Workflows

### Workflow 1: Test Code

```
User: Run the tests and show me the results

Claude will:
1. Invoke the "Run" action in IDE
2. Wait for tests to complete (smart-wait)
3. Capture a screenshot of test results
4. Show you the screenshot
```

### Workflow 2: Code Review

```
User: Show me the differences in Main.kt compared to what I was editing earlier

Claude will:
1. Open Main.kt
2. Read the file content
3. Compare it to previous state (if cached in conversation)
4. Summarize changes
```

### Workflow 3: Code Refactoring

```
User: Help me rename this function from oldName to newName

Claude will:
1. Navigate to the function
2. Use IDE refactor action
3. Confirm changes
4. Update code
```

---

## Best Practices

### Performance

- **Keep prompts specific:** "Open src/Main.kt" is faster than "Find the main entry point file"
- **Combine operations:** Instead of "Take screenshot, then click OK", do it in two messages where Claude sees the screenshot first
- **Use timeouts:** Long waits can be slow; use smaller timeout values when possible

### Reliability

- **Verify IDE state:** Ask Claude to check what's on screen before complex operations
- **Error recovery:** If something fails, ask Claude to take a screenshot and show you the state
- **Close dialogs:** If dialogs appear unexpectedly, have Claude close them before retrying

### Security

- **Local only by default:** IntelliJ Agent Controller connects to `localhost:8580` only
- **Firewall:** Don't expose the robot server port to the internet
- **Trust:** Only connect to MCPs you trust, as they can control your IDE

---

## Troubleshooting

### MCP Tool Not Available

**Problem:** Claude/Cursor says "Tool not found"

**Solutions:**
1. Restart Claude/Cursor
2. Verify MCP config syntax is correct (JSON formatting)
3. Check that the JAR path exists: `ls cli/build/libs/intellij-agent-controller.jar`
4. Rebuild if needed: `./gradlew shadowJar`

### IntelliJ Connection Failed

**Problem:** Tool mentions "Connection refused" or "Cannot reach IDE"

**Solutions:**
1. Ensure IntelliJ is running
2. Verify Robot Server plugin is installed (Preferences → Plugins → search "Robot Server")
3. Check port 8580 isn't blocked: `lsof -i :8580` (macOS/Linux) or `netstat -ano | findstr :8580` (Windows)
4. Try manual connection first: `java -jar intellij-agent-controller.jar connect`

### Slow Responses

**Problem:** Claude commands take 10+ seconds

**Reasons & Solutions:**
- **Stdio overhead:** Normal for MCP stdio. Try SSE transport for faster responses.
- **Large IDE:** Many components slow operations. Close dialogs, minimize clutter.
- **Network latency:** If remote IDE, switch to SSE transport or SSH tunnel
- **Debug mode:** Remove `--debug` flag (it's slower)

### Partial Results

**Problem:** Claude shows incomplete data ("Timeout during operation")

**Solutions:**
1. Break the task into smaller steps
2. Increase timeout in MCP options if supported
3. Take intermediate screenshots to verify progress
4. Use `smart-wait` more aggressively: `wait --timeout 30000`

---

## Advanced

### Building Custom Prompts

Create a system prompt for Claude that emphasizes IDE-specific knowledge:

```
You are an expert IDE assistant with deep knowledge of:
- IntelliJ IDEA UI patterns and controls
- XPath selectors for finding components: //JButton[@text='...'], //JTextField, etc.
- Common IDE actions: Run, Build, Debug, Refactor, Navigate
- IDE behavior: indexing, builds, test runs

When controlling the IDE:
1. First take a screenshot to see the current state
2. Locate components using XPath patterns
3. Perform actions step-by-step
4. Verify results with screenshots between major steps
5. Use wait-for commands when expecting UI changes
```

### Integration with Scripts

Combine shell scripts with MCP:

```bash
#!/bin/bash
# 1. Automate setup via shell
./gradlew shadowJar

# 2. Start MCP server in background
java -jar cli/build/libs/intellij-agent-controller.jar mcp-server --transport stdio &
MCP_PID=$!

# 3. Connect Claude Desktop or other tool

# 4. When done, stop the server
kill $MCP_PID
```

---

## Further Reading

- [Official MCP Specification](https://modelcontextprotocol.io)
- [Claude API Documentation](https://claude.ai/docs)
- [Cursor Documentation](https://cursor.sh/docs)
- [CLI Reference](CLI_REFERENCE.md)
