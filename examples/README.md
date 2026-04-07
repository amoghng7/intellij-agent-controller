# Examples

Runnable examples demonstrating IntelliJ Agent Controller workflows.

## CLI Examples

Bash scripts (.sh) showing common automation patterns. Windows users can run these in WSL or use the .bat equivalents.

### 01-basic-find-click.sh

Find a UI component and click it. Demonstrates XPath queries and basic interaction.

```bash
./examples/cli/01-basic-find-click.sh
```

**What it does:**
- Connects to IDE
- Takes a screenshot to see current state
- Finds the "Run" button
- Clicks it
- Verifies the click with another screenshot

### 02-editor-operations.sh

Open a file, read/modify content, save it.

```bash
./examples/cli/02-editor-operations.sh
```

**What it does:**
- Opens a file (src/Main.kt)
- Reads the editor content
- Formats the code using IDE action
- Takes a screenshot of the result

### 03-project-navigation.sh

Navigate within a project, open files, explore structure.

```bash
./examples/cli/03-project-navigation.sh
```

**What it does:**
- Gets current project path
- Opens multiple files
- Uses IDE navigation commands
- Takes screenshots at each step

### 04-actions-and-dialogs.sh

Invoke IDE actions and interact with dialogs.

```bash
./examples/cli/04-actions-and-dialogs.sh
```

**What it does:**
- Invokes IDE actions (Build, Find, etc.)
- Detects and closes dialogs
- Waits for operations to complete
- Shows how to handle IDE state changes

## Running CLI Examples

### Prerequisites

1. **Build the JAR:**
   ```bash
   cd /path/to/intellij-agent-controller
   ./gradlew shadowJar
   ```

2. **Start IntelliJ with Robot Server:**
   - Open IntelliJ IDEA
   - Install Robot Server plugin if not already done
   - Open or create a project

3. **Set up example environment:**
   ```bash
   cd examples/cli
   # Make scripts executable
   chmod +x *.sh
   ```

### Running an Example

```bash
# From cli examples directory
cd examples/cli
./01-basic-find-click.sh

# Or from project root
examples/cli/01-basic-find-click.sh

# With debug output
JAR_PATH="/path/to/cli/build/libs/intellij-agent-controller.jar" DEBUG=1 ./01-basic-find-click.sh
```

### Customizing Examples

Edit the script to:
- Change target components (XPath selectors)
- Modify file paths
- Add additional steps
- Capture outputs to files

---

## MCP Examples

AI agent integration guides for Claude Desktop and Cursor.

### 01-claude-setup.md

Complete guide for setting up Claude Desktop integration.

1. Build the JAR
2. Configure Claude Desktop
3. Restart Claude
4. Try the example prompts

**Example prompts to try:**
```
"Take a screenshot and tell me what's open"
"Find and click the Run button"
"Open src/Main.kt and show me the first 10 lines"
"Execute the ReformatCode action on the current file"
```

### 02-cursor-setup.md

Complete guide for setting up Cursor IDE integration.

Same as Claude but with Cursor-specific settings.

### example-prompts.txt

Copy-paste ready prompts for testing:

```
# Check IDE state
"Is IntelliJ running and what project is open?"

# File operations
"Open the file src/Main.kt and read its content"
"Format the code in the current editor"

# UI automation
"Take a screenshot and describe what you see"
"Find and click any button labeled 'Build'"

# Complex workflows
"Build the project and show me the results"
"Navigate to the MyClass file and open it"
```

---

## Windows Users

### Scripts in WSL

Run bash scripts in Windows Subsystem for Linux:
```bash
wsl
cd /mnt/c/path/to/intellij-agent-controller/examples/cli
./01-basic-find-click.sh
```

### PowerShell Alternatives

For each .sh script, there's a .bat equivalent:

```batch
cd examples\cli
01-basic-find-click.bat
```

### Direct Commands

Or run commands directly:
```powershell
$JAR = "cli/build/libs/intellij-agent-controller.jar"
java -jar $JAR screenshot --output screenshot.png
java -jar $JAR find "//JButton[@text='Run']"
java -jar $JAR click "//JButton[@text='Run']"
```

---

## Structure

```
examples/
├── README.md              # This file
├── cli/
│   ├── 01-basic-find-click.sh    # Find & click example
│   ├── 01-basic-find-click.bat   # Windows batch version
│   ├── 02-editor-operations.sh   # Edit file example
│   ├── 02-editor-operations.bat
│   ├── 03-project-navigation.sh  # Navigate project example
│   ├── 03-project-navigation.bat
│   ├── 04-actions-and-dialogs.sh # Actions & dialogs example
│   └── 04-actions-and-dialogs.bat
└── mcp/
    ├── 01-claude-setup.md        # Claude Desktop setup
    ├── 02-cursor-setup.md        # Cursor setup
    └── example-prompts.txt       # Copy-paste prompts
```

---

## Tips

### Debugging Scripts

Add error handling to scripts:

```bash
set -e  # Exit on error
set -x  # Print each command
```

### Capturing Output

Save command output for analysis:

```bash
java -jar "$JAR" find-all --json > components.json
java -jar "$JAR" screenshot --output current.png
```

### Using Variables

Make scripts flexible:

```bash
JAR_PATH="${JAR_PATH:-cli/build/libs/intellij-agent-controller.jar}"
XPATH="${1:-//JButton[@text='OK']}"
java -jar "$JAR_PATH" click "$XPATH"
```

### Combining Commands

Chain operations:

```bash
java -jar "$JAR" click "//JButton[@text='Run']" && \
java -jar "$JAR" smart-wait && \
java -jar "$JAR" screenshot --output results.png
```

---

## Contributing Examples

Have a useful example? Submit a PR with:
- Working script (shell or batch)
- README comment explaining what it does
- Use case / workflow it demonstrates

→ **[CLI Reference](../docs/CLI_REFERENCE.md)**  
→ **[Getting Started](../docs/GETTING_STARTED.md)**  
→ **[MCP Integration](../docs/MCP_INTEGRATION.md)**
