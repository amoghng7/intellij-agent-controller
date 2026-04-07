# Architecture & Design

Deep technical overview of IntelliJ Agent Controller architecture, design patterns, and how components interact.

## High-Level Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                  IntelliJ Agent Controller                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐         ┌──────────────┐                       │
│  │   CLI Tool   │         │  MCP Server  │                       │
│  │  (Clikt)     │         │  (Ktor)      │                       │
│  └──────┬───────┘         └──────┬───────┘                       │
│         │                        │                               │
│         └────────────┬───────────┘                               │
│                      │                                           │
│         ┌────────────▼────────────┐                              │
│         │   Core Tools Module     │                              │
│         │  (Pure Kotlin, no deps) │                              │
│         │                         │                              │
│         │ • ComponentTools        │                              │
│         │ • EditorTools           │                              │
│         │ • IdeTools              │                              │
│         │ • WidgetTools           │                              │
│         │ • InteractionTools      │                              │
│         │ • DialogTools           │                              │
│         │ • DiagnosticTools       │                              │
│         │ • WaitTools             │                              │
│         │ • JsTools               │                              │
│         │ • JcefTools             │                              │
│         └────────────┬────────────┘                              │
│                      │                                           │
│         ┌────────────▼────────────┐                              │
│         │  ConnectionManager      │                              │
│         │  (RemoteRobot HTTP)     │                              │
│         └────────────┬────────────┘                              │
│                      │ HTTP                                      │
│         ┌────────────▼────────────┐                              │
│         │   Robot Server Port     │                              │
│         │   (IntelliJ Plugin)     │                              │
│         │   Default: 8580         │                              │
│         └────────────┬────────────┘                              │
│                      │                                           │
│         ┌────────────▼────────────┐                              │
│         │   IntelliJ IDEA IDE     │                              │
│         │   (UI Components)       │                              │
│         └─────────────────────────┘                              │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Module Organization

### core/

**Purpose:** Pure, reusable tool implementations with no CLI or MCP dependencies.

**Key files:**
- `tools/` — 10 tool classes (ComponentTools, EditorTools, IdeTools, etc.)
- `models/` — Data models (ComponentInfo, EditorInfo, ScreenshotResult, etc.)
- `ConnectionManager.kt` — Manages HTTP connection to robot-server
- `exceptions/` — Custom exception types

**Responsibilities:**
- Communicate with remote-robot plugin via HTTP
- Parse and return structured data (JSON models)
- Implement core IDE automation logic

### cli/

**Purpose:** Command-line interface using Clikt framework.

**Key files:**
- `Main.kt` — CLI entry point, argument parsing, global options
- `commands/` — 13 command group files (LifecycleCommands, ComponentCommands, etc.)
- `output/` — Output formatting (text, JSON, structured tables)

**Responsibilities:**
- Parse CLI arguments and options
- Route commands to core tool implementations
- Format and display results for terminal users

**Build output:** Fat JAR at `cli/build/libs/intellij-agent-controller.jar`

### mcp-server/

**Purpose:** Model Context Protocol server for AI agent integration.

**Key files:**
- `McpServer.kt` — MCP protocol implementation, tool registration
- `transport/` — Stdio and SSE transport implementations

**Responsibilities:**
- Expose core tools as MCP tools
- Handle stdio/SSE protocol communication
- Serialize/deserialize tool calls and results

---

## Communication Flow

### 1. CLI Command Execution

```
User enters: java -jar intellij-agent-controller.jar click "//JButton[@text='OK']"
    ↓
Main.kt parses arguments
    ↓
ClickCommand (in InteractionCommands.kt) receives "//JButton[@text='OK']"
    ↓
ClickCommand calls: InteractionTools.click(xpath)
    ↓
InteractionTools.click(xpath) calls:
    ConnectionManager.post("/api/interaction/click", {"xpath": "//JButton..."})
    ↓
HTTP POST to robot-server on localhost:8580
    ↓
Robot Server (IntelliJ plugin) executes click via IntelliJ UI Test Robot
    ↓
Response: {"success": true}
    ↓
CLI outputs: "✓ Clicked component"
```

### 2. MCP Tool Execution

```
Claude asks: "Click the OK button for me"
    ↓
Claude's MCP client calls:
    Tool: click_component
    Params: {xpath: "//JButton[@text='OK']"}
    ↓
McpServer.kt receives tool call (stdin)
    ↓
McpServer maps to Core tool: InteractionTools.click(xpath)
    ↓
Same as CLI flow (steps 5-9 above)
    ↓
McpServer serializes result to JSON
    ↓
Response sent to Claude via stdout
    ↓
Claude receives: {"success": true, "message": "Button clicked"}
    ↓
Claude responds to user: "Done! I clicked the OK button."
```

---

## Core Tools System

### Tool Base Architecture

All tools follow a consistent pattern:

```kotlin
// Example: ComponentTools
class ComponentTools(private val connectionManager: ConnectionManager) {
    
    fun findAll(): List<ComponentInfo> {
        // Makes HTTP call via connectionManager
        // Returns parsed list of ComponentInfo objects
    }
    
    fun find(xpath: String): ComponentInfo? {
        // Find single component
    }
    
    fun exists(xpath: String): Boolean {
        // Check existence
    }
}
```

**Key principle:** Tools are stateless — all state is maintained by the IDE. Each call is independent.

### Available Tools

| Tool | Responsibility | Key Methods |
|------|---|---|
| **ComponentTools** | Find, inspect UI components | `findAll()`, `find()`, `exists()`, `getProperty()` |
| **EditorTools** | File management and editing | `openFile()`, `getEditorText()`, `setEditorText()` |
| **IdeTools** | IDE-level operations | `invokeAction()`, `openProject()`, `getProjectPath()` |
| **InteractionTools** | User interactions | `click()`, `type()`, `pressKey()`, `select()` |
| **WidgetTools** | Specific widget types | `clickButton()`, `checkCheckbox()`, `selectCombobox()` |
| **DialogTools** | Dialog management | `findDialog()`, `closeDialog()` |
| **WaitTools** | Synchronization | `waitForComponent()`, `waitForCondition()`, `smartWait()` |
| **DiagnosticTools** | Troubleshooting | `screenshot()`, `getLogs()`, `getHierarchy()` |
| **JsTools** | JavaScript execution | `executeJs()` |
| **JcefTools** | Browser component control | `findBrowser()`, `executeJs()`, `getUrl()` |

---

## RemoteRobot Technology

[IntelliJ UI Test Robot](https://github.com/JetBrains/intellij-ui-test-robot) is JetBrains' framework for automating UI testing. It provides:

- **Component Discovery:** Access to IntelliJ's component tree via XPath
- **Interaction APIs:** Click, type, select, drag-drop operations
- **Robot Server Plugin:** HTTP server plugin in the IDE exposing UI APIs

### How It Works

1. **IDE starts with plugin:** Robot Server plugin runs in the IDE process
2. **HTTP Endpoints:** Plugin exposes endpoints on port 8580 (configurable)
3. **XPath Selectors:** All component queries use XPath against the component tree
4. **Remote Execution:** Each action executes in the IDE's main thread

### Example HTTP Requests

```
GET /api/components [?xpath=...]
  ↓ Returns list of components matching XPath

POST /api/components/{id}/click
  ↓ Clicks the component

POST /api/editor/open
  ↓ Opens a file in the editor
```

---

## ConnectionManager

Central HTTP client for all tool communication.

### Responsibilities

- Maintain persistent HTTP connection to robot-server
- Retry logic for failed requests
- Serialize/deserialize JSON payloads
- Handle timeouts and connection errors
- Debug logging

### Implementation

```kotlin
class ConnectionManager(val host: String = "127.0.0.1", val port: Int = 8580) {
    
    fun get(endpoint: String, params: Map<String, String>? = null): JsonObject {
        // GET request to http://host:port/endpoint?params
    }
    
    fun post(endpoint: String, body: JsonObject): JsonObject {
        // POST request with JSON body
    }
    
    fun isConnected(): Boolean {
        // Check if robot-server is reachable
    }
}
```

---

## Data Models

Immutable data classes representing different types of information:

### ComponentInfo

Represents a UI component.

```kotlin
data class ComponentInfo(
    val id: String,
    val className: String,
    val text: String?,
    val visible: Boolean,
    val enabled: Boolean,
    val bounds: Rectangle,
    val properties: Map<String, String>
)
```

### EditorInfo

Represents the current editor state.

```kotlin
data class EditorInfo(
    val fileName: String,
    val filePath: String,
    val caretLine: Int,
    val caretColumn: Int,
    val selectionStart: Int,
    val selectionEnd: Int,
    val lineCount: Int
)
```

### ScreenshotResult

Represents a screenshot.

```kotlin
data class ScreenshotResult(
    val imagePath: String?,  // If saved to file
    val imageBase64: String?,  // If returned as base64
    val width: Int,
    val height: Int
)
```

---

## Error Handling

### Exception Hierarchy

```
ConnectionException
  ├─ ConnectionTimeoutException
  ├─ ConnectionRefusedException
  └─ InvalidResponseException

ComponentException
  ├─ ComponentNotFoundException
  └─ InvalidXPathException

EditorException
  ├─ NoEditorException
  └─ FileLockedException

IdeException
  ├─ ProjectNotFoundException
  └─ ActionNotFoundException
```

### Error Recovery

Tools follow a pattern:

1. **Attempt operation** with timeout
2. **On failure:** Collect error details
3. **Return meaningful error message** to user/agent
4. **No retry:** Let caller decide (CLI may retry, MCP may suggest alternative)

---

## Performance Characteristics

### Latency

- **Simple operations** (find, click): 50-200ms
- **File operations** (open file): 100-500ms
- **Complex operations** (take screenshot): 500ms-2s
- **IDE indexing waits** (smart-wait): Can be 5-60s depending on project size

### Optimization Strategies

1. **Batch Operations:** Use `find-all` once instead of multiple `find` calls
2. **XPath Efficiency:** Specific XPath (e.g., `//JButton[@text='OK']`) is faster than broad queries
3. **Smart Waits:** Use `wait-for-condition` to avoid polling from CLI
4. **Screenshot Optimization:** Only capture needed regions, not full screen

### Concurrent Operations

- **Single connection:** Only one robot-server connection at a time (IDE limitation)
- **Sequential:** All operations must be sequential
- **No parallelization:** Cannot run multiple tool invocations simultaneously against same IDE

---

## Extensibility

### Adding a New Tool

1. **Create new tool class** in `core/src/main/kotlin/.../tools/`
   ```kotlin
   class MyNewTools(private val connectionManager: ConnectionManager) {
       fun doSomething(): Result {
           return connectionManager.post("/api/mynew/action", mapOf(...))
       }
   }
   ```

2. **Register in CLI** (`cli/.../commands/MyNewCommands.kt`)
   ```kotlin
   class MyNewCommand : CliktCommand(name = "my-command") {
       override fun run() {
           val tools = MyNewTools(connectionManager)
           val result = tools.doSomething()
           echo(result)
       }
   }
   ```

3. **Register in MCP** (`mcp-server/.../McpServer.kt`)
   ```kotlin
   registerTool("my_new_tool") {
       val tools = MyNewTools(connectionManager)
       tools.doSomething()
   }
   ```

### Adding a New Command

1. File: `cli/.../commands/MyCommandGroup.kt`
2. Extend `CliktCommand`
3. Register in `Main.kt` subcommand list

---

## Testing Approach

### Unit Tests (Missing)

Ideally, tests would:
- Mock ConnectionManager
- Verify tool methods call correct endpoints
- Validate response parsing

### Integration Tests (Recommended)

To add integration tests:

1. Start IntelliJ with robot-server plugin
2. Create test fixtures (open test project, etc.)
3. Run CLI commands and verify results

Example:
```kotlin
@Test
fun testClickButton() {
    // Start IDE with robot-server
    // Execute: click("//JButton[@text='OK']")
    // Wait for action
    // Verify state changed
}
```

---

## Design Patterns

### Separation of Concerns

- **core/**: Pure business logic, no UI/framework dependencies
- **cli/**: UI/terminal concerns (Clikt, formatting)
- **mcp-server/**: Protocol concerns (MCP serialization)

This makes core tools reusable across any interface.

### Stateless Operations

- No global state except ConnectionManager
- Each operation is independent
- Easy to test, debug, and reason about

### Fail-Fast

- Operations timeout quickly (default 5000ms)
- Early error detection helps IDE, doesn't hang
- Errors are explicit and descriptive

---

## Future Improvements

1. **Connection Pooling:** Support multiple IDE instances
2. **Async Operations:** Non-blocking tool calls
3. **Event Streaming:** Subscribe to IDE events (file changes, build status)
4. **Recording & Playback:** Record sequences of commands, replay later
5. **IDE Compatibility Matrix:** Official list of tested IDE versions
6. **Rate Limiting:** Throttle requests to avoid IDE overload
7. **Caching:** Cache component trees between requests for performance

---

## References

- [IntelliJ UI Test Robot GitHub](https://github.com/JetBrains/intellij-ui-test-robot)
- [Clikt Documentation](https://ajalt.github.io/clikt/)
- [MCP Specification](https://modelcontextprotocol.io)
- [Ktor Documentation](https://ktor.io/)
