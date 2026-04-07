A CLI Tool and MCP server which lets AI Agents control JetBrains IntelliJ IDE using [IntelliJ UI Test Robot](https://github.com/JetBrains/intellij-ui-test-robot) framework.

## General Instructions

- Always research [IntelliJ UI Test Robot](https://github.com/JetBrains/intellij-ui-test-robot) and its capabilities before implementing any feature or fixing bugs.

## Project Structure

```
intellij-agent-controller/
├── core/           # Core tool implementations (no CLI/MCP dependency)
│   └── tools/      # ComponentTools, EditorTools, IdeTools, JsTools,
│                   # InteractionTools, WidgetTools, JcefTools,
│                   # DialogTools, DiagnosticTools, WaitTools
├── cli/            # Clikt-based CLI interface (fat JAR via Shadow plugin)
│   └── commands/   # One file per command group (LifecycleCommands, etc.)
├── mcp-server/     # MCP server (stdio + SSE transport)
└── bin/            # Wrapper scripts (ij, ij.bat)
```

## Tech Stack

- **Language:** Kotlin 2.1.0, JVM 21
- **Build:** Gradle 8.13 (Kotlin DSL), Shadow plugin for fat JAR
- **CLI:** Clikt 5.0.3
- **Robot:** remote-robot 0.11.23, remote-fixtures 0.11.23, ide-launcher 0.11.23
- **MCP:** modelcontextprotocol/kotlin-sdk 0.7.0, Ktor (Netty) for SSE transport
- **Serialization:** Gson (CLI), kotlinx-serialization-json (MCP)

## Build Commands

```bash
./gradlew build          # Build all modules
./gradlew shadowJar      # Build fat JAR (cli/build/libs/intellij-agent-controller.jar)
./gradlew :cli:run --args="--help"    # Run CLI
./gradlew :mcp-server:run --args="stdio"  # Run MCP server standalone
```

## Architecture

- **core** module contains all tool logic, depends on remote-robot/remote-fixtures/ide-launcher
- **cli** module depends on core + mcp-server, provides Clikt commands, produces a Shadow JAR
- **mcp-server** module depends on core, exposes tools via MCP protocol (stdio/SSE)
- Communication with IDE is via HTTP to the robot-server plugin (default port 8580)
- XPath is the primary UI component selector mechanism