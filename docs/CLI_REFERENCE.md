# CLI Command Reference

Complete reference for all IntelliJ Agent Controller CLI commands.

## Usage

All commands follow this pattern:

```bash
java -jar intellij-agent-controller.jar [GLOBAL_OPTIONS] <COMMAND> [COMMAND_OPTIONS]
```

## Global Options

Available before any command:

| Option | Default | Description |
|--------|---------|-------------|
| `--host` | `127.0.0.1` | Robot server host address |
| `--port` | `8580` | Robot server port |
| `--json` | `false` | Output results in JSON format |
| `--debug` | `false` | Enable debug logging (HTTP requests, timing) |
| `--help` | — | Show help text |

### Examples

```bash
# Connect to a remote IDE
java -jar intellij-agent-controller.jar --host 192.168.1.100 --port 8580 screenshot

# Get JSON output
java -jar intellij-agent-controller.jar --json find-all

# Enable debug logging
java -jar intellij-agent-controller.jar --debug click "//JButton[@text='OK']"
```

---

## Lifecycle Commands

### launch

Download and launch an IDE instance.

```bash
java -jar intellij-agent-controller.jar launch [OPTIONS]
```

Options:
- `--ide <IDE>` — IDE type: `IC` (Community), `IU` (Ultimate). Default: `IC`
- `--version <VERSION>` — IDE version (e.g., `2024.1`, `2024.2`). Default: latest stable
- `--path <PATH>` — Custom download/install directory

Examples:
```bash
# Launch latest Community Edition
java -jar intellij-agent-controller.jar launch

# Launch specific version
java -jar intellij-agent-controller.jar launch --ide IC --version 2024.1

# Custom install path
java -jar intellij-agent-controller.jar launch --path /opt/intellij
```

### connect

Connect to an already-running IDE with Robot Server plugin.

```bash
java -jar intellij-agent-controller.jar connect [OPTIONS]
```

Options:
- `--host <HOST>` — Robot server host. Default: `127.0.0.1`
- `--port <PORT>` — Robot server port. Default: `8580`

Examples:
```bash
# Local IDE
java -jar intellij-agent-controller.jar connect

# Remote IDE
java -jar intellij-agent-controller.jar connect --host 192.168.1.100 --port 8580
```

### status

Get current IDE and connection status.

```bash
java -jar intellij-agent-controller.jar status
```

Output (JSON with `--json`):
```json
{
  "connected": true,
  "ideVersion": "2024.1.1",
  "projectOpen": true,
  "projectPath": "/Users/you/MyProject"
}
```

### shutdown

Gracefully shut down the IDE.

```bash
java -jar intellij-agent-controller.jar shutdown
```

### restart

Restart the IDE.

```bash
java -jar intellij-agent-controller.jar restart
```

---

## Component Commands

### find-all

List all visible UI components.

```bash
java -jar intellij-agent-controller.jar find-all [OPTIONS]
```

Options:
- `--json` — Output as JSON (useful for parsing)
- `--xpath <XPATH>` — Filter by XPath pattern

Output (with `--json`):
```json
[
  {
    "id": "component_1",
    "className": "JButton",
    "text": "OK",
    "visible": true,
    "enabled": true,
    "bounds": {"x": 100, "y": 200, "width": 50, "height": 25}
  }
]
```

### find

Find a single component by XPath.

```bash
java -jar intellij-agent-controller.jar find "<XPATH>"
```

Examples:
```bash
# Find button by text
java -jar intellij-agent-controller.jar find "//div[@class='JButton' and @text='OK']"

# Find any enabled button
java -jar intellij-agent-controller.jar find "//JButton[@enabled='true']"

# Find component by index
java -jar intellij-agent-controller.jar find "//JButton[0]"
```

### component-exists

Check if a component exists.

```bash
java -jar intellij-agent-controller.jar component-exists "<XPATH>"
```

Returns exit code `0` if found, `1` if not.

```bash
java -jar intellij-agent-controller.jar component-exists "//JButton[@text='Build']" && echo "Build button found"
```

### has-text

Check if a component contains specific text.

```bash
java -jar intellij-agent-controller.jar has-text "<XPATH>" "<TEXT>"
```

### get-property

Get a component property value.

```bash
java -jar intellij-agent-controller.jar get-property "<XPATH>" "<PROPERTY>"
```

Properties: `className`, `text`, `visible`, `enabled`, `bounds`, `value`

Examples:
```bash
java -jar intellij-agent-controller.jar get-property "//JButton[0]" "text"
java -jar intellij-agent-controller.jar get-property "//JTextField" "value"
```

---

## Interaction Commands

### click

Click a component.

```bash
java -jar intellij-agent-controller.jar click "<XPATH>"
```

### double-click

Double-click a component.

```bash
java -jar intellij-agent-controller.jar double-click "<XPATH>"
```

### right-click

Right-click a component (contexts menu).

```bash
java -jar intellij-agent-controller.jar right-click "<XPATH>"
```

### type

Type text into a focused input.

```bash
java -jar intellij-agent-controller.jar type "<TEXT>"
```

Note: Automatically focuses the active input field first.

```bash
java -jar intellij-agent-controller.jar click "//JTextField[@id='searchBox']"
java -jar intellij-agent-controller.jar type "my search query"
```

### press-key

Press a keyboard key or key combination.

```bash
java -jar intellij-agent-controller.jar press-key "<KEY>"
```

Key names: `enter`, `tab`, `escape`, `backspace`, `delete`, `shift`, `ctrl`, `alt`, `cmd`

Combinations (macOS uses `cmd`, others use `ctrl`):
```bash
java -jar intellij-agent-controller.jar press-key "ctrl+s"      # Save
java -jar intellij-agent-controller.jar press-key "ctrl+z"      # Undo
java -jar intellij-agent-controller.jar press-key "shift+tab"   # Unindent
java -jar intellij-agent-controller.jar press-key "enter"       # Return key
```

---

## Widget Commands

Control specific widget types.

### button

Click a button.

```bash
java -jar intellij-agent-controller.jar button "<XPATH>"
```

### checkbox

Interact with a checkbox.

```bash
java -jar intellij-agent-controller.jar checkbox "<XPATH>" [OPTIONS]
```

Options:
- `--action check|uncheck|toggle` — Default: `toggle`

### combobox

Select an item from a combobox (dropdown).

```bash
java -jar intellij-agent-controller.jar combobox "<XPATH>" [OPTIONS]
```

Options:
- `--item <VALUE>` — Item to select (required)

### textbox

Get or set a text field value.

```bash
java -jar intellij-agent-controller.jar textbox "<XPATH>" [OPTIONS]
```

Options:
- `--text <VALUE>` — Set text value
- No options: Get current value

Examples:
```bash
# Get text
java -jar intellij-agent-controller.jar textbox "//JTextField[@name='searchField']"

# Set text
java -jar intellij-agent-controller.jar textbox "//JTextField" --text "new value"

# Clear and set
java -jar intellij-agent-controller.jar textbox "//JTextField" --text ""
```

### tree

Interact with tree components.

```bash
java -jar intellij-agent-controller.jar tree "<XPATH>" [OPTIONS]
```

Options:
- `--action expand|collapse|select|paths` — Default: `paths` (list all paths)

Examples:
```bash
# Expand a tree node
java -jar intellij-agent-controller.jar tree "//JTree" --action expand

# Collapse a tree node
java -jar intellij-agent-controller.jar tree "//JTree" --action collapse

# List all tree paths
java -jar intellij-agent-controller.jar tree "//JTree" --action paths
```

### table

Interact with table components.

```bash
java -jar intellij-agent-controller.jar table "<XPATH>" [OPTIONS]
```

Options:
- `--action read|click` — Default: `read`
- `--row <ROW>` — Row index
- `--column <COL>` — Column index

Examples:
```bash
# Read entire table
java -jar intellij-agent-controller.jar table "//JTable" --action read

# Click a cell
java -jar intellij-agent-controller.jar table "//JTable" --action click --row 2 --column 1
```

---

## Editor Commands

### open-file

Open a file in the editor.

```bash
java -jar intellij-agent-controller.jar open-file "<PATH>"
```

Examples:
```bash
java -jar intellij-agent-controller.jar open-file "src/main/kotlin/Main.kt"
java -jar intellij-agent-controller.jar open-file "/absolute/path/to/File.kt"
```

### get-editor-text

Get the current editor content.

```bash
java -jar intellij-agent-controller.jar get-editor-text
```

### set-editor-text

Replace the entire editor content.

```bash
java -jar intellij-agent-controller.jar set-editor-text "<TEXT>"
```

### editor-info

Get detailed editor information.

```bash
java -jar intellij-agent-controller.jar editor-info
```

Output (JSON):
```json
{
  "fileName": "Main.kt",
  "filePath": "/Users/you/project/src/Main.kt",
  "caretLine": 5,
  "caretColumn": 10,
  "selectionStart": 50,
  "selectionEnd": 150,
  "selectedText": "selected content",
  "lineCount": 200
}
```

### editor-action

Execute an IDE editor action.

```bash
java -jar intellij-agent-controller.jar editor-action "<ACTION>"
```

Common actions:
- `ReformatCode` — Format active file
- `RenameElement` — Rename symbol
- `FindInPath` — Open find dialog
- `GotoClass` — Go to class
- `GotoFile` — Go to file

Examples:
```bash
java -jar intellij-agent-controller.jar editor-action "ReformatCode"
java -jar intellij-agent-controller.jar editor-action "GotoClass"
```

---

## IDE Commands

### invoke-action

Invoke any IDE action by ID.

```bash
java -jar intellij-agent-controller.jar invoke-action "<ACTION_ID>"
```

Common actions:
- `Run` — Run the project
- `Build` — Build the project
- `Stop` — Stop running process
- `DebugClass` — Debug current file
- `FindInPath` — Find in all files

Examples:
```bash
java -jar intellij-agent-controller.jar invoke-action "Run"
java -jar intellij-agent-controller.jar invoke-action "Build"
```

### open-project

Open a project.

```bash
java -jar intellij-agent-controller.jar open-project "<PATH>"
```

### close-project

Close the current project.

```bash
java -jar intellij-agent-controller.jar close-project
```

### get-project-path

Get the current project path.

```bash
java -jar intellij-agent-controller.jar get-project-path
```

### navigate

Navigate to a target (file, class, symbol, or action).

```bash
java -jar intellij-agent-controller.jar navigate "<TARGET>" [OPTIONS]
```

Options:
- `--type file|class|symbol|action` — Navigation type

Examples:
```bash
# Navigate to a file
java -jar intellij-agent-controller.jar navigate "Main.kt" --type file

# Navigate to a class
java -jar intellij-agent-controller.jar navigate "MyClass" --type class

# Navigate to a symbol (function, variable)
java -jar intellij-agent-controller.jar navigate "myFunction" --type symbol
```

---

## Dialog Commands

### find-dialog

Find an open dialog.

```bash
java -jar intellij-agent-controller.jar find-dialog [OPTIONS]
```

Options:
- `--title <TITLE>` — Filter by dialog title

### close-dialog

Close a dialog.

```bash
java -jar intellij-agent-controller.jar close-dialog [OPTIONS]
```

Options:
- `--action ok|cancel|close` — Close action. Default: `close`

### balloon-notification

Get balloon notifications.

```bash
java -jar intellij-agent-controller.jar balloon-notification
```

---

## Diagnostic Commands

### screenshot

Capture a screenshot.

```bash
java -jar intellij-agent-controller.jar screenshot [OPTIONS]
```

Options:
- `--output <PATH>` — Save image file
- `--component <XPATH>` — Screenshot only a component
- `--base64` — Output image as base64 instead of file

Examples:
```bash
java -jar intellij-agent-controller.jar screenshot --output screen.png

java -jar intellij-agent-controller.jar screenshot \
  --component "//EditorTabbedContainer" \
  --output editor.png

java -jar intellij-agent-controller.jar screenshot --base64
```

### get-logs

Get IDE logs.

```bash
java -jar intellij-agent-controller.jar get-logs [OPTIONS]
```

Options:
- `--lines <N>` — Number of lines to retrieve. Default: `100`

### get-hierarchy

Dump the component hierarchy.

```bash
java -jar intellij-agent-controller.jar get-hierarchy [OPTIONS]
```

Options:
- `--format json|html` — Output format. Default: `json`
- `--component <XPATH>` — Specific component subtree

---

## Wait Commands

### wait-for

Wait for a component to appear or meet a condition.

```bash
java -jar intellij-agent-controller.jar wait-for "<XPATH>" [OPTIONS]
```

Options:
- `--timeout <MS>` — Max wait time in milliseconds. Default: `5000`
- `--condition visible|enabled` — Condition to wait for. Default: `visible`

Examples:
```bash
# Wait up to 10 seconds for a button to appear
java -jar intellij-agent-controller.jar wait-for "//JButton[@text='Next']" --timeout 10000

# Wait for a component to be enabled
java -jar intellij-agent-controller.jar wait-for "//JButton[@text='Submit']" --condition enabled
```

### wait-for-condition

Wait for a JavaScript condition to be true.

```bash
java -jar intellij-agent-controller.jar wait-for-condition "<JS>" [OPTIONS]
```

Options:
- `--timeout <MS>` — Max wait time. Default: `5000`

Java code execution context (available variables):
- `ProjectManager.getInstance()` — Get projects
- `FileDocumentManager.getInstance()` — Get open documents

### smart-wait

Wait for IDE to fully load (indexing complete, build complete).

```bash
java -jar intellij-agent-controller.jar smart-wait [OPTIONS]
```

Options:
- `--timeout <MS>` — Max wait time. Default: `60000`

This is useful after launching an IDE or opening a large project.

---

## JavaScript Execution

### execute-js

Execute arbitrary JavaScript in the IDE context.

```bash
java -jar intellij-agent-controller.jar execute-js "<SCRIPT>"
```

Available IDE APIs:
- `ProjectManager.getInstance()` — Access projects
- `FileDocumentManager.getInstance()` — File operations
- `Application.getInstance()` — IDE application instance

Examples:
```bash
# Get current project name
java -jar intellij-agent-controller.jar execute-js \
  "ProjectManager.getInstance().getOpenProjects()[0].getName()"

# Get open file paths
java -jar intellij-agent-controller.jar execute-js \
  "FileDocumentManager.getInstance().getOpenFiles().map(f => f.getPath()).join(', ')"
```

---

## JCEF Commands

Control embedded browser components (web editors, preview panes).

### jcef-find-browser

Find JCEF browser components.

```bash
java -jar intellij-agent-controller.jar jcef-find-browser
```

### jcef-execute-js

Execute JavaScript in a JCEF browser.

```bash
java -jar intellij-agent-controller.jar jcef-execute-js "<SCRIPT>" [OPTIONS]
```

Options:
- `--browser <XPATH>` — Specific browser component

### jcef-get-url

Get the URL of a JCEF browser.

```bash
java -jar intellij-agent-controller.jar jcef-get-url [OPTIONS]
```

Options:
- `--browser <XPATH>` — Specific browser component

---

## MCP Server

Start the MCP server for AI agent integration.

```bash
java -jar intellij-agent-controller.jar mcp-server [OPTIONS]
```

Options:
- `--transport stdio|sse` — Transport protocol. Default: `stdio`
- `--mcp-port <PORT>` — MCP server port (for SSE). Default: `3000`
- `--port <PORT>` — Robot server port. Default: `8580`
- `--host <HOST>` — Robot server host. Default: `127.0.0.1`

Examples:
```bash
# Claude Desktop (stdio transport)
java -jar intellij-agent-controller.jar mcp-server --transport stdio

# SSE transport on custom port
java -jar intellij-agent-controller.jar mcp-server --transport sse --mcp-port 3000
```

→ **[MCP Integration Guide](MCP_INTEGRATION.md)**

---

## Tips

### XPath Examples

Common XPath patterns for finding components:

```xpath
// Find any button with text "OK"
//JButton[@text='OK']

// Find first visible button
//JButton[@visible='true'][0]

// Find any enabled element with specific class
//*[@className='JPanel' and @enabled='true']

// Find by text (partial match)
//*[contains(@text, 'Search')]

// Find nested components
//JTree//JCheckBox[@enabled='true']
```

### Using with Scripts

Combine commands in bash scripts:

```bash
#!/bin/bash
JAR="cli/build/libs/intellij-agent-controller.jar"

# Take screenshot
java -jar "$JAR" screenshot --output before.png

# Click a button
java -jar "$JAR" click "//JButton[@text='Build']"

# Wait for build to complete
java -jar "$JAR" smart-wait --timeout 120000

# Take another screenshot
java -jar "$JAR" screenshot --output after.png
```

→ **[Examples](../examples/)**
