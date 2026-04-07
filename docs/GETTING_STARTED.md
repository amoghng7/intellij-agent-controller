# Getting Started

## Installation

### Prerequisites

- **Java 21+** — Download from [java.com](https://www.java.com) or use a package manager
- **Git** — For cloning the repository
- **Gradle** — Included via Gradle Wrapper (no separate installation needed)

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/amoghng7/intellij-agent-controller.git
cd intellij-agent-controller

# Build the fat JAR
./gradlew shadowJar
```

The compiled JAR is produced at:
```
cli/build/libs/intellij-agent-controller.jar
```

### Verify Installation

```bash
# Check the CLI works
java -jar cli/build/libs/intellij-agent-controller.jar --help
```

You should see a list of available commands. If you see "command not found" or similar, verify Java 21+ is installed:
```bash
java -version
```

## First Commands

### 1. Connect to a Running IDE

If you have IntelliJ IDEA running with the **Robot Server** plugin installed:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar connect \
  --host 127.0.0.1 \
  --port 8580
```

If successful, you'll see the connection status.

**Robot Server Plugin:** If the plugin isn't installed:
1. Open IntelliJ IDEA
2. Go to **Settings → Plugins**
3. Search for "Robot Server" and install
4. Restart IntelliJ

### 2. Take a Screenshot

The simplest way to verify everything works:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar screenshot \
  --output screenshot.png
```

This writes a screenshot of the current IDE to `screenshot.png`.

### 3. Launch a New IDE Instance

Instead of using a running IDE, you can launch one:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar launch \
  --ide IC \
  --version 2024.1
```

This downloads IntelliJ Community Edition (IC) and launches it. Your system must have ~3GB free disk space.

### 4. Find UI Components

List all visible UI components as JSON:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar find-all --json
```

Or find a specific component by XPath:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar find "//div[@class='JButton' and @text='OK']"
```

### 5. Click & Interact

```bash
# Click a button
java -jar cli/build/libs/intellij-agent-controller.jar click \
  "//div[@class='JButton' and @text='OK']"

# Type text (focuses active input first)
java -jar cli/build/libs/intellij-agent-controller.jar type "Hello, IDE!"

# Press a key
java -jar cli/build/libs/intellij-agent-controller.jar press-key "ctrl+s"
```

## Using with Claude Desktop or Cursor

### Step 1: Build the JAR

```bash
./gradlew shadowJar
```

### Step 2: Find Your JAR Path

```bash
# On macOS/Linux
pwd
# Returns something like: /Users/you/Projects/intellij-agent-controller
# Full JAR path: /Users/you/Projects/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar

# On Windows
cd cli\build\libs
echo %cd%
# Full path: C:\Users\You\Projects\intellij-agent-controller\cli\build\libs\intellij-agent-controller.jar
```

### Step 3: Configure Claude Desktop

Edit your Claude Desktop config:

**macOS/Linux:**
```bash
~/.claude/claude_desktop_config.json
```

**Windows:**
```
%APPDATA%\Claude\claude_desktop_config.json
```

Add the following:

```json
{
  "mcpServers": {
    "intellij": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/intellij-agent-controller/cli/build/libs/intellij-agent-controller.jar",
        "mcp-server",
        "--transport",
        "stdio",
        "--port",
        "8580"
      ]
    }
  }
}
```

Replace `/path/to/intellij-agent-controller/...` with your actual JAR path.

### Step 4: Restart Claude Desktop

Close and reopen Claude Desktop. You should see a new "🔌 MCP" indicator showing the IntelliJ tool is connected.

### Step 5: Use in Conversations

In Claude, you can now prompt:

```
Help me find the "Run" button in IntelliJ and click it
```

or

```
Open the file src/Main.kt and show me the first 10 lines
```

Claude will use your IntelliJ instance to perform these tasks.

→ **[More MCP Examples](MCP_INTEGRATION.md)**

## Troubleshooting

### "Java: command not found"

Java isn't installed or not in your PATH. Download from [java.com](https://www.java.com) or use:

**macOS:**
```bash
brew install java
```

**Ubuntu/Debian:**
```bash
sudo apt install default-jdk
```

**Windows:**
Download from Microsoft Store or [java.com](https://www.java.com)

### "Connection refused" when connecting to IDE

- Ensure IntelliJ is running
- Ensure the Robot Server plugin is installed (Settings → Plugins → search "Robot Server")
- Verify the port (default `8580`) isn't blocked by firewall
- Try: `java -jar ... connect --host 127.0.0.1 --port 8580 --debug`

### "Cannot find robot-server plugin"

The plugin may be incompatible with your IDE version. Check:
- IntelliJ version: Help → About
- Supported versions in the Robot Server plugin page

### No output or slow responses

Add `--debug` flag to see detailed logs:

```bash
java -jar cli/build/libs/intellij-agent-controller.jar screenshot --debug
```

This shows HTTP requests, timings, and errors.

→ **[Full Troubleshooting Guide](TROUBLESHOOTING.md)**  
→ **[CLI Command Reference](CLI_REFERENCE.md)**
