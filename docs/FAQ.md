# FAQ — Frequently Asked Questions

Quick answers to common questions.

## General

### What is IntelliJ Agent Controller?

A tool that lets you (or AI agents like Claude) automate IntelliJ IDEA through a CLI or AI-friendly MCP interface. It communicates with the IDE via the [IntelliJ UI Test Robot](https://github.com/JetBrains/intellij-ui-test-robot) framework.

### What can I do with it?

- Open files, edit code, run tests
- Control IDE UI (click buttons, type in text fields)
- Take screenshots and inspect components
- Execute IDE actions (build, format, refactor)
- Use with Claude Desktop or Cursor to automate tasks with natural language

### Is it free?

Yes, Apache 2.0 licensed. Open source.

### What IDE versions does it support?

Any IntelliJ-based IDE with the Robot Server plugin installed. Tested with:
- IntelliJ Community Edition 2023.1+
- IntelliJ Ultimate 2023.1+
- Android Studio
- PyCharm, WebStorm, etc. (any IntelliJ platform)

### Does it work on macOS, Linux, Windows?

Yes to all three. Tested regularly on all platforms.

---

## Installation & Setup

### Do I need to install anything besides Java?

Just Java 21+. The tool downloads IntelliJ if you use the `launch` command.

### How do I install the Robot Server plugin?

In IntelliJ:
1. Settings → Plugins
2. SearchRobot Server"
3. Click Install
4. Restart IDE

If you use the `launch` command, the plugin is automatically installed.

### I already have IntelliJ running. What do I do?

Just connect to it:
```bash
java -jar intellij-agent-controller.jar connect
```

Make sure the Robot Server plugin is installed first.

### Can I use it with IntelliJ running on a remote server?

Yes:
```bash
java -jar intellij-agent-controller.jar connect --host 192.168.1.100 --port 8580
```

Or set up an SSH tunnel:
```bash
ssh -L 8580:remote-machine:8580 user@remote-machine
java -jar intellij-agent-controller.jar connect --host 127.0.0.1 --port 8580
```

---

## Usage

### How do I control the IDE from the command line?

Basic example:
```bash
java -jar intellij-agent-controller.jar click "//JButton[@text='OK']"
```

See [CLI Reference](docs/CLI_REFERENCE.md) for all commands.

### How do I use this with Claude?

1. Build the JAR: `./gradlew shadowJar`
2. Edit Claude Desktop config (~/.claude/claude_desktop_config.json)
3. Add the MCP server with the JAR path
4. Restart Claude
5. Ask Claude to control your IDE

See [MCP Integration Guide](docs/MCP_INTEGRATION.md) for details.

### What is XPath and how do I use it?

XPath is a query language for finding UI elements. Examples:

```xpath
// Find button with text "OK"
//JButton[@text='OK']

// Find any visible button
//JButton[@visible='true']

// Find by class name
//JPanel[@className='SettingsPanel']

// Find partial text match
//*[contains(@text, 'Search')]
```

Learn XPath: [w3schools.com/xml/xpath_syntax.asp](https://www.w3schools.com/xml/xpath_syntax.asp)

### Can I run multiple IDEs at the same time?

Yes, but you control them sequentially (one at a time). Each IDE needs a different port:

```bash
java -jar intellij-agent-controller.jar connect --port 8580  # IDE 1
java -jar intellij-agent-controller.jar connect --port 8581  # IDE 2
```

### How do I wait for something to happen?

Use `wait-for`:
```bash
# Wait for button to appear
java -jar intellij-agent-controller.jar wait-for "//JButton[@text='Next']"

# Wait for IDE to finish indexing
java -jar intellij-agent-controller.jar smart-wait
```

---

## Troubleshooting

### Connection refused — what do I do?

See [Troubleshooting Guide](docs/TROUBLESHOOTING.md#connection-issues). Usually:
1. Ensure IntelliJ is running
2. Install Robot Server plugin
3. Verify port 8580 is not blocked

### Screenshot shows nothing or is black

The IDE might be:
- Minimized or backgrounded
- Entirely covered by another window
- Not yet started

Try:
```bash
java -jar intellij-agent-controller.jar status
```

This will say if connected or give an error.

### "Component not found" — what does it mean?

The XPath selector didn't match any component. Try:

1. Take a screenshot to verify component is visible
2. Use `find-all` to see all components
3. Check your XPath syntax

### My automation is very slow

See [Performance Guide](docs/PERFORMANCE.md). Common causes:
- IDE is indexing (wait with `smart-wait`)
- Large IDE project (use specific XPath queries)
- MCP stdio transport (use CLI for speed)

### Commands work in CLI but not in MCP (Claude)

Common issues:
1. MCP server isn't running (restart Claude)
2. JAR path in config is wrong
3. IDE connection failed (check firewall, port)

Add `--debug` to see errors:
```bash
java -jar intellij-agent-controller.jar --debug click "//JButton[@text='OK']"
```

---

## Features & Capabilities

### Can it refactor code?

Not directly, but it can:
- TriggerRefactoring actions (`invoke-action "RenameElement"`)
- Interact with refactoring dialogs
- Combination: Claude can guide the refactoring

### Can it run tests?

Yes:
```bash
java -jar intellij-agent-controller.jar invoke-action "Run"
java -jar intellij-agent-controller.jar smart-wait  # Wait for completion
java -jar intellij-agent-controller.jar screenshot  # Verify results
```

### Can it access the debugger?

Yes, the debugger is controlled like any other UI component. You can:
- Click step buttons
- Set breakpoints (through UI or commands)
- Inspect variables

### Can I automate Android Studio?

Yes, it's built on the IntelliJ Platform. Works the same way.

### Can I right-click context menus?

Yes:
```bash
java -jar intellij-agent-controller.jar right-click "//JTree//TreeNode[@text='Main.kt']"
java -jar intellij-agent-controller.jar click "//MenuItem[@text='Refactor']"
```

### Can it handle modal dialogs?

Yes, it can find and close them:
```bash
java -jar intellij-agent-controller.jar find-dialog --title "Settings"
java -jar intellij-agent-controller.jar close-dialog --action "ok"
```

---

## Architecture & Design

### Why use HTTP instead of JetBrains' officially supported methods?

The IntelliJ UI Test Robot and robot-server plugin are JetBrains' official offering for remote IDE testing and automation. It's well-documented, stable, and widely used for integration testing.

### Can I see the source code?

Yes, it's on GitHub: [amoghng7/intellij-agent-controller](https://github.com/amoghng7/intellij-agent-controller)

### What languages does it support?

The tool works with any IntelliJ-based IDE, so it supports any language:
- Java, Kotlin
- Python (PyCharm)
- JavaScript (WebStorm)
- C++ (CLion)
- Go, Rust, etc.

### Is this a Jetbrains-supported project?

No, it's a community project. But it uses JetBrains' open source robot-server plugin, which is maintained by JetBrains.

---

## API & Integration

### Can I use this as a library in my own project?

Yes, import the `core` module:
```kotlin
implementation(project(":core"))
```

Then use the tool classes directly. See [API Reference](docs/API_REFERENCE.md).

### Can I add custom tools?

Yes, create a new tool class in `core/tools/`, extend CLI in `cli/commands/`, and register in MCP. See [Contributing Guide](docs/CONTRIBUTING.md).

### Does it work with CI/CD pipelines?

Yes, it's a CLI tool. Can be run in CI/CD:

```bash
# GitHub Actions example
- name: Run IDE automation
  run: |
    java -jar cli.jar launch --ide IC
    java -jar cli.jar open-file "src/Main.kt"
    java -jar cli.jar screenshot --output test.png
```

Note: Some CI environments may not allow GUI apps. Docker with X11 forwarding required.

### Can I use this on Linux without a display server?

Not directly (IDE needs a UI). Options:
- Use X11 forwarding over SSH
- Run IntelliJ in Docker with VNC
- Use headless mode if available

---

## Limitations

### What can't it do?

- **Automated typing in text editors with special formatting:** Rich text editors with complex rendering may not respond to `type` command predictably
- **Interact with non-IDE windows:** Only controls IntelliJ window
- **Access IDE's internal state directly:** Only through public actions/APIs exposed by robot-server
- **Modify IDE behavior without restarting:** Some configuration changes require IDE restart
- **Parallel operations:** Only one operation at a time per IDE instance
- **Multiple IDE instances easily:** No built-in session management

### What are the performance limits?

- **Latency:** Typical 100-500ms per operation
- **Throughput:** ~10 operations/second realistic max
- **Screenshot size:** Limited by IDE window size and resolution
- **Timeout:** Default 5s, can be increased but not indefinitely

### Does it work offline?

The tool runs locally, but IntelliJ itself needs internet for some features (plugin download, code analysis updates). For basic automation, no internet needed.

---

## Getting Help

### How do I report a bug?

1. Reproduce the issue with `--debug` flag
2. Collect output, IDE version, Java version
3. Open GitHub issue with details

### How do I request a feature?

Open a GitHub discussion or issue with:
- Use case description
- Desired command/behavior
- Why it would be useful

### Where's the documentation?

- [Getting Started](docs/GETTING_STARTED.md)
- [CLI Reference](docs/CLI_REFERENCE.md)
- [MCP Integration](docs/MCP_INTEGRATION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [API Reference](docs/API_REFERENCE.md)
- [Contributing](docs/CONTRIBUTING.md)
- [Performance Tips](docs/PERFORMANCE.md)

### Where can I find examples?

See the [examples/](examples/) folder for:
- CLI scripts demonstrating workflows
- MCP integration examples (Claude, Cursor)
- Ready-to-use prompts

---

## Legal & Licensing

### What license is this under?

Apache License 2.0. See [LICENSE](LICENSE).

### Can I use this commercially?

Yes, Apache 2.0 allows commercial use. See license for details.

### Can I modify the code?

Yes, Apache 2.0 allows modifications. If you distribute modified versions, include license notice and describe changes.

### What about dependencies?

All dependencies are compatible with commercial use. Check individual dependency licenses if needed.

---

## Miscellaneous

### Why is it called "agent" controller?

Because it's designed to be controlled by AI agents (like Claude). It exposes an MCP interface that agents can understand and use naturally.

### Can I use this to control my team's IDE instance?

Not recommended for security reasons. If you do, ensure the robot-server port is not exposed to the internet, and use SSH tunneling with strong auth.

### Does it work with JetBrains Fleet (new IDE)?

Not yet. Fleet is different architecture from IntelliJ Platform. Support can be added if there's demand.

### Will this work with future IDE versions?

Likely yes, as long as IntelliJ Platform remains backward-compatible. May need updates if plugin APIs change significantly.

---

Still have questions? Check the [Contributing](docs/CONTRIBUTING.md) guide or open a GitHub discussion!
