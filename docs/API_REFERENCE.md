# API Reference

Core module API for extending IntelliJ Agent Controller.

## Module Overview

The `core` module contains all tool implementations and models. It has no CLI or MCP dependencies, making it reusable in any context.

## Architecture

```
core/
├── tools/              # 10 Tool classes
├── models/             # Data classes
├── ConnectionManager   # HTTP client
└── exceptions/         # Custom exceptions
```

## ConnectionManager

Central HTTP client for all robot-server communication.

### Class: ConnectionManager

```kotlin
class ConnectionManager(
    val host: String = "127.0.0.1",
    val port: Int = 8580
)
```

### Methods

#### isConnected(): Boolean

Check if robot-server is reachable.

```kotlin
if (connectionManager.isConnected()) {
    println("IDE is reachable")
}
```

#### get(endpoint: String, params: Map<String, String>? = null): JsonObject

HTTP GET request.

```kotlin
val components = connectionManager.get(
    "/api/components",
    mapOf("xpath" to "//JButton[@text='OK']")
)
```

#### post(endpoint: String, body: JsonObject): JsonObject

HTTP POST request.

```kotlin
val result = connectionManager.post(
    "/api/interaction/click",
    JsonObject().apply {
        addProperty("xpath", "//JButton[@text='OK']")
    }
)
```

#### delete(endpoint: String): JsonObject

HTTP DELETE request.

```kotlin
val result = connectionManager.delete("/api/connection")
```

---

## Tool Classes

All tools follow this pattern:

```kotlin
class MyTools(private val connectionManager: ConnectionManager) {
    fun doSomething(): Result {
        // Implementation
    }
}
```

### ComponentTools

Find, inspect, and query UI components.

```kotlin
class ComponentTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun findAll(): List<ComponentInfo>
fun find(xpath: String): ComponentInfo?
fun exists(xpath: String): Boolean
fun hasText(xpath: String, text: String): Boolean
fun getProperty(xpath: String, property: String): String?
```

**Example:**

```kotlin
val tools = ComponentTools(connectionManager)
val buttons = tools.findAll()
    .filter { it.className == "JButton" }
    .filter { it.enabled }

buttons.forEach { button ->
    println("${button.text}: ${button.bounds}")
}
```

### EditorTools

File management and editor operations.

```kotlin
class EditorTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun openFile(filePath: String): Boolean
fun getEditorText(): String
fun setEditorText(text: String): Boolean
fun getEditorInfo(): EditorInfo
fun executeAction(actionId: String): Boolean
```

**Example:**

```kotlin
val tools = EditorTools(connectionManager)
tools.openFile("src/Main.kt")
val content = tools.getEditorText()
tools.setEditorText(content.replace("old", "new"))
```

### IdeTools

IDE-level operations (projects, actions, navigation).

```kotlin
class IdeTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun invokeAction(actionId: String): Boolean
fun openProject(projectPath: String): Boolean
fun closeProject(): Boolean
fun getProjectPath(): String
fun navigate(target: String, type: String): Boolean
```

**Example:**

```kotlin
val tools = IdeTools(connectionManager)
val projectPath = tools.getProjectPath()
println("Project: $projectPath")
tools.invokeAction("Run")
```

### InteractionTools

User input simulation (clicks, typing, key presses).

```kotlin
class InteractionTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun click(xpath: String): Boolean
fun doubleClick(xpath: String): Boolean
fun rightClick(xpath: String): Boolean
fun type(text: String): Boolean
fun pressKey(key: String): Boolean
fun select(xpath: String, item: String): Boolean
```

**Example:**

```kotlin
val tools = InteractionTools(connectionManager)
tools.click("//JButton[@text='File']")
tools.click("//MenuItem[@text='Open']")
tools.type("myfile.txt")
tools.pressKey("enter")
```

### WidgetTools

Control specific widget types (buttons, checkboxes, tables, etc.).

```kotlin
class WidgetTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun button(xpath: String): Boolean
fun checkbox(xpath: String, action: String = "toggle"): Boolean
fun combobox(xpath: String, item: String): Boolean
fun textbox(xpath: String, text: String? = null): String?
fun tree(xpath: String, action: String = "paths"): List<String>
fun table(xpath: String, action: String = "read"): TableData
```

**Example:**

```kotlin
val tools = WidgetTools(connectionManager)
tools.button("//JButton[@text='OK']")
tools.checkbox("//JCheckBox[@name='enable']", "check")
tools.combobox("//ComboBox", "Option 1")
val treeNodes = tools.tree("//ProjectTree", "paths")
```

### WaitTools

Synchronization and conditional waits.

```kotlin
class WaitTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun waitFor(xpath: String, timeout: Long = 5000, condition: String = "visible"): Boolean
fun waitForCondition(js: String, timeout: Long = 5000): Boolean
fun smartWait(timeout: Long = 60000): Boolean
```

**Example:**

```kotlin
val tools = WaitTools(connectionManager)
// Wait for button to appear
if (tools.waitFor("//JButton[@text='Next']", timeout = 10000)) {
    println("Button appeared")
}

// Wait for custom condition
val ready = tools.waitForCondition(
    "ProjectManager.getInstance().getOpenProjects().length > 0",
    timeout = 30000
)

// Wait for IDE indexing
tools.smartWait()
```

### DialogTools

Dialog and notification management.

```kotlin
class DialogTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun findDialog(title: String? = null): DialogInfo?
fun closeDialog(action: String = "close"): Boolean
fun getBalloonNotifications(): List<NotificationInfo>
```

**Example:**

```kotlin
val tools = DialogTools(connectionManager)
val dialog = tools.findDialog("Settings")
if (dialog != null) {
    tools.closeDialog("ok")
}
```

### DiagnosticTools

Diagnostics, logging, and debugging.

```kotlin
class DiagnosticTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun screenshot(outputPath: String? = null): ScreenshotResult
fun getLogs(lines: Int = 100): String
fun getHierarchy(format: String = "json"): String
```

**Example:**

```kotlin
val tools = DiagnosticTools(connectionManager)
val screenshot = tools.screenshot("current.png")
println("Screenshot: ${screenshot.imagePath}")

val logs = tools.getLogs()
println(logs)

val hierarchy = tools.getHierarchy("json")
```

### JsTools

Execute arbitrary JavaScript in IDE context.

```kotlin
class JsTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun executeJs(script: String): String
```

**Example:**

```kotlin
val tools = JsTools(connectionManager)
val projectName = tools.executeJs(
    "ProjectManager.getInstance().getOpenProjects()[0].getName()"
)
println("Project: $projectName")
```

### JcefTools

Control embedded browser components (web editors, previews).

```kotlin
class JcefTools(private val connectionManager: ConnectionManager)
```

#### Methods

```kotlin
fun findBrowser(): JcefBrowserInfo?
fun executeJs(script: String, browser: String? = null): String
fun getUrl(browser: String? = null): String
```

**Example:**

```kotlin
val tools = JcefTools(connectionManager)
val browser = tools.findBrowser()
if (browser != null) {
    val url = tools.getUrl(browser.id)
    println("Browser URL: $url")
}
```

---

## Data Models

All immutable (`data class`), serializable to/from JSON.

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

data class Rectangle(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
```

### EditorInfo

Represents current editor state.

```kotlin
data class EditorInfo(
    val fileName: String,
    val filePath: String,
    val caretLine: Int,
    val caretColumn: Int,
    val selectionStart: Int,
    val selectionEnd: Int,
    val selectedText: String?,
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

### DialogInfo

Represents a dialog.

```kotlin
data class DialogInfo(
    val id: String,
    val title: String,
    val modal: Boolean,
    val visible: Boolean
)
```

### NotificationInfo

Represents a notification.

```kotlin
data class NotificationInfo(
    val id: String,
    val title: String,
    val content: String,
    val type: String  // "error", "warning", "info"
)
```

### TableData

Represents table structure and content.

```kotlin
data class TableData(
    val rowCount: Int,
    val columnCount: Int,
    val headers: List<String>,
    val rows: List<List<String>>
)
```

---

## Exceptions

Custom exception hierarchy:

```kotlin
open class IdeException(message: String, cause: Throwable? = null)
    : Exception(message, cause)

class ConnectionException(message: String, cause: Throwable? = null)
    : IdeException(message, cause)

class ConnectionTimeoutException(message: String)
    : ConnectionException(message)

class ComponentNotFoundException(xpath: String)
    : IdeException("Component not found: $xpath")

class EditorException(message: String)
    : IdeException(message)

class IdeActionException(action: String)
    : IdeException("Action failed: $action")
```

**Example:**

```kotlin
try {
    val tools = ComponentTools(connectionManager)
    tools.click("//NonExistentButton")
} catch (e: ComponentNotFoundException) {
    println("Button not found: ${e.message}")
}
```

---

## Usage Examples

### Example 1: Basic Automation

```kotlin
val connectionManager = ConnectionManager("127.0.0.1", 8580)

// Check connection
if (!connectionManager.isConnected()) {
    println("IDE not reachable")
    return
}

// Interact with IDE
val componentTools = ComponentTools(connectionManager)
val buttons = componentTools.findAll()
    .filter { it.className == "JButton" }

buttons.forEach { button ->
    println("Found button: ${button.text}")
}
```

### Example 2: File Editing

```kotlin
val editorTools = EditorTools(connectionManager)
val interactionTools = InteractionTools(connectionManager)

// Open file
editorTools.openFile("src/Main.kt")

// Get current content
val content = editorTools.getEditorText()

// Replace text
val updated = content.replace("TODO", "DONE")
editorTools.setEditorText(updated)

// Execute reformat action
editorTools.executeAction("ReformatCode")
```

### Example 3: Complex Workflow

```kotlin
val componentTools = ComponentTools(connectionManager)
val interactionTools = InteractionTools(connectionManager)
val waitTools = WaitTools(connectionManager)
val ideTools = IdeTools(connectionManager)

// Open file dialog
ideTools.invokeAction("Ctrl+O")

// Wait for dialog
waitTools.waitFor("//FileChooserPanel", timeout = 5000)

// Type filename
interactionTools.type("test.kt")

// Click OK
interactionTools.click("//JButton[@text='OK']")

// Wait for file to open
waitTools.smartWait()
```

---

## Integration with Projects

### Adding to Your Project

1. Add core module as dependency:
   ```kotlin
   implementation(project(":core"))
   ```

2. Import and use in your code:
   ```kotlin
   import com.github.intellijagent.core.tools.*
   import com.github.intellijagent.core.models.*
   ```

3. Create ConnectionManager and tools:
   ```kotlin
   val connectionManager = ConnectionManager()
   val componentTools = ComponentTools(connectionManager)
   ```

---

## Best Practices

1. **Check connection first:**
   ```kotlin
   if (!connectionManager.isConnected()) {
       // Handle connection error
   }
   ```

2. **Use error handling:**
   ```kotlin
   try {
       componentTools.click(xpath)
   } catch (e: ComponentNotFoundException) {
       // Handle not found
   }
   ```

3. **Wait for expected state changes:**
   ```kotlin
   ideTools.invokeAction("Build")
   waitTools.smartWait()  // Wait for build to complete
   ```

4. **Keep XPath queries specific:**
   ```kotlin
   // Good: Specific
   componentTools.find("//JButton[@text='OK' and @enabled='true']")
   
   // Bad: Too broad
   componentTools.find("//JButton")
   ```

---

## References

- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [IntelliJ UI Test Robot](https://github.com/JetBrains/intellij-ui-test-robot)
- [XPath Syntax](https://www.w3schools.com/xml/xpath_syntax.asp)
