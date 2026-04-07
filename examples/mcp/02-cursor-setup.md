# Cursor IDE Integration Guide

Complete setup guide for using IntelliJ Agent Controller with Cursor IDE.

## Prerequisites

1. **Cursor IDE** installed (download from [www.cursor.com](https://www.cursor.com))
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
pwd  # Copy this output
# Example: /Users/you/projects/intellij-agent-controller
```

**Windows (PowerShell):**
```powershell
(Get-Location).Path  # Copy this output
# Example: C:\Users\You\projects\intellij-agent-controller
```

### 3. Open Cursor Settings

1. Open Cursor IDE
2. **Cursor → Settings** (Mac) or **File → Preferences** (Windows/Linux)
3. Navigate to **Features → MCP** (or search for "MCP")

### 4. Add IntelliJ MCP Server

In the MCP settings:

1. Click **Add MCP Tool** (or similar button)
2. Choose **Command** as the type
3. Fill in:
   - **Command:** `java`
   - **Args:** (see below)

**Args (macOS/Linux example):**
```
-jar /Users/you/projects/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar mcp-server --transport stdio
```

**Args (Windows example):**
```
-jar C:\Users\You\projects\intellij-agent-controller\cli\build\libs\intellij-agent-controller.jar mcp-server --transport stdio
```

4. Click **Add** or **Save**

### 5. Verify Connection

1. Restart Cursor IDE
2. Look for **Tools** or **MCP** indicator showing IntelliJ is available
3. Test by asking in composer: "Take a screenshot"

## Testing

### Test Prompts

Try these in Cursor's composer:

```
Take a screenshot of IntelliJ and describe what's on screen.
```

```
What's the current project path in IntelliJ?
```

```
Click the Run button in IntelliJ.
```

```
Open src/Main.kt and show me the content.
```

## Using with Cursor

### Agent Mode

Cursor's composer can use MCP tools in Agent mode:

1. Open **Composer**
2. Type your request
3. Press **Agent Mode** or similar
4. Cursor will automatically use IntelliJ tool when appropriate

### Example Workflows

**Code Navigation:**
```
In IntelliJ, navigate to the MyClass file and show me what's there.
```

**Running Tests:**
```
Click the test runner button in IntelliJ and show me the results.
```

**Code Analysis:**
```
Open the main file and help me understand the structure.
```

**Build Verification:**
```
Build the project in IntelliJ and tell me if it succeeded.
```

## Troubleshooting

### MCP Tool Not Appearing

1. **Restart Cursor:** Close and fully reopen
2. **Check args syntax:** Make sure the full command is correct
3. **Verify JAR path:** Ensure path to JAR is absolute and correct
4. **Test manually:**
   ```bash
   java -jar /path/to/cli/build/libs/intellij-agent-controller.jar mcp-server --transport stdio
   # Should start without errors
   ```

### Tool Doesn't Respond

1. **Check IntelliJ is running** with Robot Server plugin
2. **Take manual screenshot first:**
   ```bash
   java -jar /path/to/cli/build/libs/intellij-agent-controller.jar screenshot
   ```
3. **Check Cursor logs:** Look in Cursor's settings for logs directory

### Slow Responses

- MCP stdio has overhead (normal)
- If IDE is busy (indexing/building), operations will be slower
- Ask Claude to check IDE state first before complex operations

## Differences from Claude Desktop

| Feature | Claude Desktop | Cursor |
|---------|---|---|
| **Setup** | Config file | Settings GUI |
| **Transport** | Stdin/stdout | Stdin/stdout (same) |
| **Performance** | Same | Same |
| **Integration** | Standalone | Built into IDE |

## Tips

1. **Direct questions:** "What's visible in IntelliJ right now?"
2. **Sequential operations:** Break into steps, verify between each
3. **Screenshots for verification:** "Take a screenshot first"
4. **Explicit tool use:** "Using the IntelliJ tool, ..."

## Advanced: Custom Instructions

In Cursor settings, you can add instructions to the AI about how to use IntelliJ:

```
You have access to the IntelliJ Agent Controller tool to control JetBrains IntelliJ IDE.

Always:
1. First take a screenshot to see the current state
2. Describe what you observe
3. Ask before making changes
4. Use specific XPath selectors (//JButton[@text='OK'])
5. Verify after operations

Available operations:
- Click UI elements
- Type text
- Execute IDE actions
- Open/read files
- Take screenshots
- Find components by XPath
```

---

See also:
- [MCP Integration Guide](../../docs/MCP_INTEGRATION.md)
- [Claude Desktop Setup](01-claude-setup.md)
- [CLI Reference](../../docs/CLI_REFERENCE.md)
