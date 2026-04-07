# Claude Desktop Integration Guide

Complete setup guide for using IntelliJ Agent Controller with Claude Desktop.

## Prerequisites

1. **Claude Desktop** installed (download from [claude.ai/download](https://claude.ai/download))
2. **IntelliJ IDEA** running with Robot Server plugin
3. **Java 21+** installed
4. **IntelliJ Agent Controller** repository cloned

## Setup Steps

### 1. Build the Fat JAR

```bash
cd /path/to/intellij-agent-controller
./gradlew shadowJar
```

This creates: `cli/build/libs/intellij-agent-controller.jar`

### 2. Find Your JAR Path

**macOS/Linux:**
```bash
pwd
# Output: /Users/you/projects/intellij-agent-controller
# JAR path: /Users/you/projects/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar
```

**Windows (PowerShell):**
```powershell
(Get-Location).Path
# Output: C:\Users\You\projects\intellij-agent-controller
# JAR path: C:\Users\You\projects\intellij-agent-controller\cli\build\libs\intellij-agent-controller.jar
```

### 3. Edit Claude Desktop Config

**macOS/Linux:**
```bash
# Use your preferred editor
nano ~/.claude/claude_desktop_config.json
# Or use Finder: Cmd+Shift+. to show hidden files, navigate to ~/.claude/
```

**Windows:**
```powershell
# Using Notepad
notepad $env:APPDATA\Claude\claude_desktop_config.json

# Or use WSL
wsl nano ~/.claude/claude_desktop_config.json
```

### 4. Add MCP Server Configuration

Insert this configuration (replace path with your actual JAR path):

```json
{
  "mcpServers": {
    "intellij": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/you/projects/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar",
        "mcp-server",
        "--transport",
        "stdio"
      ]
    }
  }
}
```

**Full example config:**
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/Users/you/Documents"]
    },
    "intellij": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/you/projects/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar",
        "mcp-server",
        "--transport",
        "stdio"
      ]
    }
  }
}
```

### 5. Restart Claude Desktop

- **Close completely:** Cmd+Q (macOS) or Alt+F4 (Windows)
- **Wait 5 seconds**
- **Reopen Claude Desktop**

### 6. Verify Connection

You should see a 🔌 **MCP** indicator showing available tools. Look for "intellij" in the available tools.

If not visible:
- Check config JSON syntax (use JSONLint)
- Verify JAR path is correct and file exists
- Check Claude logs: `~/.claude/logs/mcp-server.log`

## Testing

### Basic Test Prompt

Copy and paste this into Claude:

```
Take a screenshot of the IDE and describe what's on screen.
```

Claude should:
1. Use the IntelliJ tool
2. Return a screenshot
3. Describe the IDE state

### More Test Prompts

```
What files are open in the IDE?
```

```
Click the Run button and take a screenshot.
```

```
Open the file src/Main.kt and show me the first 20 lines.
```

```
Format the code in the current editor using the IDE's formatter.
```

```
Get the current project path.
```

## Using with Claude

### Natural Language Control

You can now ask Claude to control your IDE directly:

**Example 1: Code Review**
```
Please review the code in src/MyClass.kt. First, open it and show me what's there.
```

Claude will:
1. Open the file
2. Read the content
3. Provide review feedback

**Example 2: Build & Test**
```
Build the project and take a screenshot of the build output.
```

Claude will:
1. Click the Build button (or invoke action)
2. Wait for completion
3. Capture and show results

**Example 3: Complex Workflow**
```
Help me set up a new file. Create a new Kotlin file, name it TestFile.kt, 
and show me the editor with the template that IntelliJ generates.
```

Claude will:
1. Navigate menus
2. Interact with dialogs
3. Show the result

## Troubleshooting

### "Tool not found" or MCP icon missing

**Problem:** IntelliJ tool isn't available in Claude.

**Solutions:**
1. Fully close Claude (not just minimize):
   ```bash
   # macOS
   killall "Claude"
   # Windows (PowerShell)
   Stop-Process -Name "Claude" -Force
   ```

2. Check config file syntax:
   - Verify JSON is valid (use JSONLint)
   - Check all paths are correct
   - Ensure file is saved

3. Check Claude logs:
   ```bash
   tail -50 ~/.claude/logs/mcp-server.log
   ```

4. Verify JAR exists:
   ```bash
   ls -la /path/to/cli/build/libs/intellij-agent-controller.jar
   ```

### "Connection refused" during tool use

**Problem:** Claude tries to use the tool but can't reach IDE.

**Solutions:**
1. Ensure IntelliJ is running
2. Ensure Robot Server plugin is installed (Settings → Plugins)
3. Verify port 8580 isn't blocked (check firewall)
4. Test manual connection:
   ```bash
   java -jar cli/build/libs/intellij-agent-controller.jar connect
   ```

### Tool times out or very slow

**Problem:** Commands take 10+ seconds to execute.

**Possible causes:**
- IDE is indexing (appears as progress bar)
- IDE is building (status bar shows build progress)
- MCP stdio overhead (normal)
- Network latency if IDE is remote

**Solutions:**
- Wait for IDE to finish indexing/building
- Use CLI directly for performance-critical workflows
- Ask Claude to check IDE state first: "Take a screenshot first to see if the IDE is busy"

### Claude responds but doesn't interact with IDE

**Problem:** Claude acknowledges but doesn't use the tool.

**Cause:** Claude might not understand it should use the tool.

**Solution:** Be explicit:
```
Using the IntelliJ tool, take a screenshot and describe the IDE.
```

Or try a different phrasing:
```
What's currently visible in IntelliJ IDEA?
```

## Advanced: Custom Prompts

Create a custom prompt to "teach" Claude how to use IntelliJ better:

**In Claude → Settings → Custom Instructions:**

```
You have access to an IntelliJ Agent Controller tool that can:
- Control IntelliJ IDEA IDE directly
- Take screenshots, click buttons, type text, execute actions
- Open files, read/modify editor content
- Find UI components using XPath queries

When the user asks about code or IDE tasks:
1. First take a screenshot to see current state
2. Describe what you see
3. If action needed, use the tool to interact with IDE
4. Take screenshots between major steps to verify
5. Report results back to user

Always verify IDE state before and after operations.
```

## Examples

### Example: Help reviewing a file

**User:** "Show me the code in src/models/User.kt"

**Claude workflow:**
1. Takes screenshot (sees IDE state)
2. Opens file using tool
3. Reads editor content via tool
4. Shows user the code
5. Can ask clarifying questions

### Example: Automated refactoring

**User:** "Rename the 'oldFunctionName' function to 'newFunctionName' throughout the project"

**Claude workflow:**
1. Navigate to function
2. Invoke IDE Refactor → Rename
3. Enter new name
4. Execute refactor
5. Show results

### Example: Setup new project

**User:** "Create a new Kotlin file in src/ and add a simple class"

**Claude workflow:**
1. Use IDE actions to create file
2. Add template code
3. Show final result
4. Ready for user to edit

## Tips

- **Be specific** about what you want: "Find and click the green Run button" not just "run code"
- **Ask for screenshots** to verify state before complex operations: "Take a screenshot first"
- **Reference 1-2 previous screenshots** in conversation for context
- **Use descriptive XPath** when appropriate: `//JButton[@text='OK']` is better than just "OK button"

---

See also:
- [MCP Integration Guide](../../docs/MCP_INTEGRATION.md)
- [CLI Reference](../../docs/CLI_REFERENCE.md)
- [Troubleshooting](../../docs/TROUBLESHOOTING.md)
