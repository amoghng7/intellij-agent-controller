package com.github.intellijagent.mcp

import com.github.intellijagent.core.ConnectionManager
import com.github.intellijagent.core.IdeConfig
import com.github.intellijagent.core.IdeLifecycleManager
import com.github.intellijagent.core.tools.*
import com.google.gson.Gson
import com.intellij.remoterobot.launcher.Ide
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private val gson = Gson()

// --- Argument helpers ---

private fun JsonObject?.str(key: String): String? =
    this?.get(key)?.jsonPrimitive?.content

private fun JsonObject?.int(key: String): Int? =
    this?.get(key)?.jsonPrimitive?.content?.toIntOrNull()

private fun JsonObject?.bool(key: String): Boolean? =
    this?.get(key)?.jsonPrimitive?.content?.toBooleanStrictOrNull()

private fun JsonObject?.strList(key: String): List<String>? =
    this?.get(key)?.jsonArray?.map { it.jsonPrimitive.content }

// --- Mutable holder for lifecycle manager ---

private class Ref<T>(var value: T)

// --- Entry point ---

fun main(args: Array<String>): Unit = runBlocking {
    val transportMode = args.firstOrNull() ?: "stdio"
    val port = args.getOrNull(1)?.toIntOrNull() ?: 3000
    val robotHost = args.getOrNull(2) ?: "127.0.0.1"
    val robotPort = args.getOrNull(3)?.toIntOrNull() ?: 8580

    val server = createMcpServer(ConnectionManager(), robotHost, robotPort)

    when (transportMode) {
        "sse" -> runSse(server, port)
        else -> runStdio(server)
    }
}

private suspend fun runStdio(server: Server) {
    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered()
    )
    server.createSession(transport)
    val done = Job()
    server.onClose { done.complete() }
    done.join()
}

private fun runSse(server: Server, port: Int) {
    embeddedServer(Netty, port = port) {
        mcp { server }
    }.start(wait = true)
}

// --- Server creation ---

private fun createMcpServer(
    conn: ConnectionManager,
    defaultHost: String,
    defaultPort: Int
): Server {
    val lifecycleRef = Ref<IdeLifecycleManager?>(null)

    val server = Server(
        serverInfo = Implementation(name = "intellij-agent-controller", version = "0.1.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = true))
        )
    )

    registerLifecycleTools(server, conn, lifecycleRef, defaultHost, defaultPort)
    registerComponentTools(server, conn)
    registerJsTools(server, conn)
    registerInteractionTools(server, conn)
    registerWidgetTools(server, conn)
    registerEditorTools(server, conn)
    registerIdeTools(server, conn)
    registerJcefTools(server, conn)
    registerDialogTools(server, conn)
    registerDiagnosticTools(server, conn, lifecycleRef)
    registerWaitTools(server, conn)

    return server
}

// --- Result helpers ---

private fun ok(data: Any? = null): CallToolResult {
    val text = when (data) {
        is String -> data
        null -> """{"status":"ok"}"""
        else -> gson.toJson(data)
    }
    return CallToolResult(content = listOf(TextContent(text = text)))
}

private fun err(e: Exception): CallToolResult =
    CallToolResult(content = listOf(TextContent(text = "Error: ${e.message}")), isError = true)

// ===== LIFECYCLE TOOLS =====

private fun registerLifecycleTools(
    server: Server,
    conn: ConnectionManager,
    lifecycleRef: Ref<IdeLifecycleManager?>,
    defaultHost: String,
    defaultPort: Int
) {
    server.addTool(
        name = "connect",
        description = "Connect to IntelliJ robot server",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("host") { put("type", "string"); put("description", "Robot server host (default: 127.0.0.1)") }
                putJsonObject("port") { put("type", "integer"); put("description", "Robot server port (default: 8580)") }
            }
        )
    ) { request ->
        try {
            val host = request.arguments.str("host") ?: defaultHost
            val port = request.arguments.int("port") ?: defaultPort
            conn.connect(host, port)
            ok(mapOf("status" to "connected", "host" to host, "port" to port))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "disconnect",
        description = "Disconnect from robot server"
    ) { _ ->
        try {
            conn.disconnect()
            ok(mapOf("status" to "disconnected"))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "status",
        description = "Get IDE status (process alive, robot server ready)"
    ) { _ ->
        try {
            val lifecycle = lifecycleRef.value
            val status = lifecycle?.getStatus()
            val health = conn.checkHealth()
            ok(mapOf(
                "connected" to conn.isConnected(),
                "healthy" to health,
                "processAlive" to (status?.processAlive ?: false),
                "robotServerReady" to (status?.robotServerReady ?: health)
            ))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "launch_ide",
        description = "Launch IntelliJ IDE with robot-server plugin",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("ide_type") { put("type", "string"); put("description", "IDE type: IC, IU, CL, WS, RM, PY, PC, RD") }
                putJsonObject("version") { put("type", "string"); put("description", "IDE version (optional)") }
                putJsonObject("build_type") { put("type", "string"); put("description", "Build type: eap or release (default: eap)") }
                putJsonObject("port") { put("type", "integer"); put("description", "Robot server port (default: 8580)") }
            },
            required = listOf("ide_type")
        )
    ) { request ->
        try {
            val ideType = parseIdeType(request.arguments.str("ide_type"))
            val version = request.arguments.str("version")
            val buildType = parseBuildType(request.arguments.str("build_type"))
            val port = request.arguments.int("port") ?: defaultPort

            val config = IdeConfig(ideType = ideType, version = version, buildType = buildType, robotServerPort = port)
            val manager = IdeLifecycleManager(config)
            manager.launch()
            lifecycleRef.value = manager
            conn.connect("127.0.0.1", port)
            ok(mapOf("status" to "launched", "ide" to ideType.name, "port" to port))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "shutdown_ide",
        description = "Shutdown the IDE"
    ) { _ ->
        try {
            lifecycleRef.value?.shutdown()
            conn.disconnect()
            ok(mapOf("status" to "shutdown"))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "restart_ide",
        description = "Restart the IDE"
    ) { _ ->
        try {
            val manager = lifecycleRef.value ?: throw IllegalStateException("No IDE has been launched")
            manager.restart()
            val port = manager.getStatus().let { defaultPort }
            conn.connect("127.0.0.1", port)
            ok(mapOf("status" to "restarted"))
        } catch (e: Exception) { err(e) }
    }
}

// ===== COMPONENT TOOLS =====

private fun registerComponentTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "find_all_components",
        description = "Find all UI components in the IDE"
    ) { _ ->
        try { ok(ComponentTools.findAll(conn.getRemoteRobot())) } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "find_component",
        description = "Find a UI component by XPath selector",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector for the component") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            ok(ComponentTools.find(conn.getRemoteRobot(), xpath))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "component_exists",
        description = "Check if a component exists",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            ok(mapOf("exists" to ComponentTools.componentExists(conn.getRemoteRobot(), xpath)))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "has_text",
        description = "Check if a component contains specific text",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector") }
                putJsonObject("text") { put("type", "string"); put("description", "Text to search for") }
            },
            required = listOf("xpath", "text")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            val text = request.arguments.str("text")!!
            ok(mapOf("hasText" to ComponentTools.hasText(conn.getRemoteRobot(), xpath, text)))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "get_property",
        description = "Get a property value from a component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector") }
                putJsonObject("property") { put("type", "string"); put("description", "Property name (e.g. Text, Name, Enabled)") }
            },
            required = listOf("xpath", "property")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            val property = request.arguments.str("property")!!
            ok(mapOf("value" to ComponentTools.getProperty(conn.getRemoteRobot(), xpath, property)))
        } catch (e: Exception) { err(e) }
    }
}

// ===== JAVASCRIPT TOOLS =====

private fun registerJsTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "execute_js",
        description = "Execute JavaScript in IDE and return result",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("script") { put("type", "string"); put("description", "JavaScript code to execute") }
                putJsonObject("component_xpath") { put("type", "string"); put("description", "Optional XPath of component context") }
                putJsonObject("run_in_edt") { put("type", "boolean"); put("description", "Run in Event Dispatch Thread (default: false)") }
            },
            required = listOf("script")
        )
    ) { request ->
        try {
            val script = request.arguments.str("script")!!
            val xpath = request.arguments.str("component_xpath")
            val edt = request.arguments.bool("run_in_edt") ?: false
            val result = JsTools.executeJs(conn.getRemoteRobot(), script, xpath, edt)
            ok(mapOf("result" to result))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "run_js",
        description = "Execute JavaScript in IDE without return value",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("script") { put("type", "string"); put("description", "JavaScript code to execute") }
                putJsonObject("component_xpath") { put("type", "string"); put("description", "Optional XPath of component context") }
                putJsonObject("run_in_edt") { put("type", "boolean"); put("description", "Run in Event Dispatch Thread (default: false)") }
            },
            required = listOf("script")
        )
    ) { request ->
        try {
            val script = request.arguments.str("script")!!
            val xpath = request.arguments.str("component_xpath")
            val edt = request.arguments.bool("run_in_edt") ?: false
            JsTools.runJs(conn.getRemoteRobot(), script, xpath, edt)
            ok()
        } catch (e: Exception) { err(e) }
    }
}

// ===== INTERACTION TOOLS =====

private fun registerInteractionTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "click",
        description = "Click a UI component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector") }
                putJsonObject("button") { put("type", "string"); put("description", "Mouse button: left, right, middle (default: left)") }
                putJsonObject("times") { put("type", "integer"); put("description", "Click count (default: 1)") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            val button = request.arguments.str("button") ?: "left"
            val times = request.arguments.int("times") ?: 1
            InteractionTools.click(conn.getRemoteRobot(), xpath, button, times)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "double_click",
        description = "Double-click a component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            InteractionTools.doubleClick(conn.getRemoteRobot(), request.arguments.str("xpath")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "right_click",
        description = "Right-click a component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            InteractionTools.rightClick(conn.getRemoteRobot(), request.arguments.str("xpath")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "type_text",
        description = "Type text into the currently focused component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("text") { put("type", "string"); put("description", "Text to type") }
            },
            required = listOf("text")
        )
    ) { request ->
        try {
            InteractionTools.typeText(conn.getRemoteRobot(), request.arguments.str("text")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "press_key",
        description = "Press a key combination (e.g. 'ctrl+s', 'enter', 'escape')",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("key") { put("type", "string"); put("description", "Key or combination (e.g. enter, escape, ctrl+s, alt+f4)") }
            },
            required = listOf("key")
        )
    ) { request ->
        try {
            InteractionTools.pressKey(conn.getRemoteRobot(), request.arguments.str("key")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "select_item",
        description = "Select an item in a list, combobox, or other selectable component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector") }
                putJsonObject("item") { put("type", "string"); put("description", "Item text to select") }
            },
            required = listOf("xpath", "item")
        )
    ) { request ->
        try {
            InteractionTools.select(conn.getRemoteRobot(), request.arguments.str("xpath")!!, request.arguments.str("item")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }
}

// ===== WIDGET TOOLS =====

private fun registerWidgetTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "click_button",
        description = "Click a button",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector for the button") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            WidgetTools.button(conn.getRemoteRobot(), request.arguments.str("xpath")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "toggle_checkbox",
        description = "Toggle, check, or uncheck a checkbox",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector for the checkbox") }
                putJsonObject("action") { put("type", "string"); put("description", "Action: check, uncheck, toggle (default: toggle)") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            val action = request.arguments.str("action") ?: "toggle"
            val checked = WidgetTools.checkbox(conn.getRemoteRobot(), request.arguments.str("xpath")!!, action)
            ok(mapOf("checked" to checked))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "select_combobox",
        description = "Select an item in a combobox or get the current selection",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector for the combobox") }
                putJsonObject("item") { put("type", "string"); put("description", "Item to select (omit to get current selection)") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            val selected = WidgetTools.combobox(conn.getRemoteRobot(), request.arguments.str("xpath")!!, request.arguments.str("item"))
            ok(mapOf("selected" to selected))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "set_textbox",
        description = "Get or set the text in a text field",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector for the text field") }
                putJsonObject("text") { put("type", "string"); put("description", "Text to set (omit to get current text)") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            val text = WidgetTools.textbox(conn.getRemoteRobot(), request.arguments.str("xpath")!!, request.arguments.str("text"))
            ok(mapOf("text" to text))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "tree_action",
        description = "Perform an action on a tree component (expand, collapse, select, list paths)",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector for the tree") }
                putJsonObject("action") { put("type", "string"); put("description", "Action: expand, collapse, select, paths, selected-paths") }
                putJsonObject("path") {
                    put("type", "array")
                    putJsonObject("items") { put("type", "string") }
                    put("description", "Tree path as list of node names")
                }
            },
            required = listOf("xpath", "action")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            val action = request.arguments.str("action")!!
            val path = request.arguments.strList("path")
            val result = WidgetTools.tree(conn.getRemoteRobot(), xpath, action, path)
            ok(result)
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "table_action",
        description = "Perform an action on a table component (read data, click cell)",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector for the table") }
                putJsonObject("action") { put("type", "string"); put("description", "Action: read, click") }
                putJsonObject("row") { put("type", "integer"); put("description", "Row index (for click action)") }
                putJsonObject("col") { put("type", "integer"); put("description", "Column index (for click action)") }
            },
            required = listOf("xpath", "action")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            val action = request.arguments.str("action")!!
            val row = request.arguments.int("row")
            val col = request.arguments.int("col")
            val result = WidgetTools.table(conn.getRemoteRobot(), xpath, action, row, col)
            ok(result)
        } catch (e: Exception) { err(e) }
    }
}

// ===== EDITOR TOOLS =====

private fun registerEditorTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "open_file",
        description = "Open a file in the editor",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("file_path") { put("type", "string"); put("description", "Absolute path to the file") }
            },
            required = listOf("file_path")
        )
    ) { request ->
        try {
            EditorTools.openFile(conn.getRemoteRobot(), request.arguments.str("file_path")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "get_editor_text",
        description = "Get the text content of the current editor"
    ) { _ ->
        try {
            ok(mapOf("text" to EditorTools.getEditorText(conn.getRemoteRobot())))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "set_editor_text",
        description = "Set the text content of the current editor",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("text") { put("type", "string"); put("description", "New text content") }
            },
            required = listOf("text")
        )
    ) { request ->
        try {
            EditorTools.setEditorText(conn.getRemoteRobot(), request.arguments.str("text")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "get_editor_info",
        description = "Get editor info (filename, path, caret position, selection)"
    ) { _ ->
        try {
            ok(EditorTools.getEditorInfo(conn.getRemoteRobot()))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "editor_action",
        description = "Execute an editor action by ID",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("action_id") { put("type", "string"); put("description", "Editor action ID (e.g. EditorCopy, EditorPaste, ReformatCode)") }
            },
            required = listOf("action_id")
        )
    ) { request ->
        try {
            EditorTools.editorAction(conn.getRemoteRobot(), request.arguments.str("action_id")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }
}

// ===== IDE TOOLS =====

private fun registerIdeTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "invoke_action",
        description = "Invoke an IDE action by ID (e.g. NewFile, OpenFile, Build)",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("action_id") { put("type", "string"); put("description", "IDE action ID") }
            },
            required = listOf("action_id")
        )
    ) { request ->
        try {
            IdeTools.invokeAction(conn.getRemoteRobot(), request.arguments.str("action_id")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "open_project",
        description = "Open a project in the IDE",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("project_path") { put("type", "string"); put("description", "Absolute path to the project") }
            },
            required = listOf("project_path")
        )
    ) { request ->
        try {
            IdeTools.openProject(conn.getRemoteRobot(), request.arguments.str("project_path")!!)
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "close_project",
        description = "Close the current project"
    ) { _ ->
        try {
            IdeTools.closeProject(conn.getRemoteRobot())
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "get_project_path",
        description = "Get the path of the currently open project"
    ) { _ ->
        try {
            ok(mapOf("path" to IdeTools.getProjectPath(conn.getRemoteRobot())))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "navigate",
        description = "Open a navigation popup (Go To File/Class/Symbol/Action) and type the target",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("target") { put("type", "string"); put("description", "Search target text") }
                putJsonObject("type") { put("type", "string"); put("description", "Navigation type: file, class, symbol, action (default: file)") }
            },
            required = listOf("target")
        )
    ) { request ->
        try {
            val target = request.arguments.str("target")!!
            val type = request.arguments.str("type") ?: "file"
            IdeTools.navigate(conn.getRemoteRobot(), target, type)
            ok()
        } catch (e: Exception) { err(e) }
    }
}

// ===== JCEF TOOLS =====

private fun registerJcefTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "jcef_find_browser",
        description = "Find JCEF browser component in the IDE"
    ) { _ ->
        try {
            ok(JcefTools.jcefFindBrowser(conn.getRemoteRobot()))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "jcef_execute_js",
        description = "Execute JavaScript in a JCEF browser component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("script") { put("type", "string"); put("description", "JavaScript code to execute in the browser") }
            },
            required = listOf("script")
        )
    ) { request ->
        try {
            val result = JcefTools.jcefExecuteJs(conn.getRemoteRobot(), request.arguments.str("script")!!)
            ok(mapOf("result" to result))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "jcef_get_url",
        description = "Get the current URL of the JCEF browser"
    ) { _ ->
        try {
            ok(mapOf("url" to JcefTools.jcefGetUrl(conn.getRemoteRobot())))
        } catch (e: Exception) { err(e) }
    }
}

// ===== DIALOG TOOLS =====

private fun registerDialogTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "find_dialog",
        description = "Find an open dialog, optionally by title",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("title") { put("type", "string"); put("description", "Dialog title to search for (optional)") }
            }
        )
    ) { request ->
        try {
            ok(DialogTools.findDialog(conn.getRemoteRobot(), request.arguments.str("title")))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "close_dialog",
        description = "Close an open dialog by clicking a button",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("action") { put("type", "string"); put("description", "Button to click: ok, cancel, close, yes, no (default: close)") }
            }
        )
    ) { request ->
        try {
            DialogTools.closeDialog(conn.getRemoteRobot(), request.arguments.str("action") ?: "close")
            ok()
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "balloon_notification",
        description = "Get current balloon notifications"
    ) { _ ->
        try {
            ok(DialogTools.balloonNotification(conn.getRemoteRobot()))
        } catch (e: Exception) { err(e) }
    }
}

// ===== DIAGNOSTIC TOOLS =====

private fun registerDiagnosticTools(server: Server, conn: ConnectionManager, lifecycleRef: Ref<IdeLifecycleManager?>) {
    server.addTool(
        name = "screenshot",
        description = "Take a screenshot of the IDE or a specific component",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("component_xpath") { put("type", "string"); put("description", "XPath of component to screenshot (optional, omit for full IDE)") }
                putJsonObject("output_path") { put("type", "string"); put("description", "File path to save screenshot (optional, returns base64 if omitted)") }
            }
        )
    ) { request ->
        try {
            val result = DiagnosticTools.screenshot(
                conn.getRemoteRobot(),
                request.arguments.str("component_xpath"),
                request.arguments.str("output_path")
            )
            ok(result)
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "get_logs",
        description = "Get IDE log output (last 500 lines)"
    ) { _ ->
        try {
            val logDir = lifecycleRef.value?.getLogDirectory()
            ok(mapOf("logs" to DiagnosticTools.getLogs(logDir)))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "get_hierarchy",
        description = "Get the component hierarchy dump from robot server",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("format") { put("type", "string"); put("description", "Output format: json or html (default: json)") }
            }
        )
    ) { request ->
        try {
            val info = conn.getConnectionInfo()
            val url = "http://${info.host}:${info.port}"
            val format = request.arguments.str("format") ?: "json"
            ok(mapOf("hierarchy" to DiagnosticTools.getHierarchyDump(url, format)))
        } catch (e: Exception) { err(e) }
    }
}

// ===== WAIT TOOLS =====

private fun registerWaitTools(server: Server, conn: ConnectionManager) {
    server.addTool(
        name = "wait_for_component",
        description = "Wait for a component to appear",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("xpath") { put("type", "string"); put("description", "XPath selector to wait for") }
                putJsonObject("timeout_ms") { put("type", "integer"); put("description", "Timeout in milliseconds (default: 10000)") }
                putJsonObject("condition") { put("type", "string"); put("description", "Condition: exists, visible, enabled (default: exists)") }
            },
            required = listOf("xpath")
        )
    ) { request ->
        try {
            val xpath = request.arguments.str("xpath")!!
            val timeout = request.arguments.int("timeout_ms")?.toLong() ?: 10000L
            val condition = request.arguments.str("condition")
            val found = WaitTools.waitForComponent(conn.getRemoteRobot(), xpath, timeout, condition)
            ok(mapOf("found" to found))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "wait_for_condition",
        description = "Wait for a JavaScript condition to become true",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("js_condition") { put("type", "string"); put("description", "JavaScript expression that returns boolean") }
                putJsonObject("timeout_ms") { put("type", "integer"); put("description", "Timeout in milliseconds (default: 10000)") }
            },
            required = listOf("js_condition")
        )
    ) { request ->
        try {
            val js = request.arguments.str("js_condition")!!
            val timeout = request.arguments.int("timeout_ms")?.toLong() ?: 10000L
            val met = WaitTools.waitForCondition(conn.getRemoteRobot(), js, timeout)
            ok(mapOf("conditionMet" to met))
        } catch (e: Exception) { err(e) }
    }

    server.addTool(
        name = "smart_wait",
        description = "Wait for IDE to finish indexing and background tasks",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("timeout_ms") { put("type", "integer"); put("description", "Timeout in milliseconds (default: 60000)") }
            }
        )
    ) { request ->
        try {
            val timeout = request.arguments.int("timeout_ms")?.toLong() ?: 60000L
            val ready = WaitTools.smartWait(conn.getRemoteRobot(), timeout)
            ok(mapOf("ready" to ready))
        } catch (e: Exception) { err(e) }
    }
}

// ===== HELPERS =====

private fun parseIdeType(type: String?): Ide {
    return when (type?.uppercase()) {
        "IC", "IDEA_COMMUNITY" -> Ide.IDEA_COMMUNITY
        "IU", "IDEA_ULTIMATE" -> Ide.IDEA_ULTIMATE
        "CL", "CLION" -> Ide.CLION
        "WS", "WEBSTORM" -> Ide.WEBSTORM
        "RM", "RUBY_MINE" -> Ide.RUBY_MINE
        "PY", "PYCHARM" -> Ide.PYCHARM
        "PC", "PYCHARM_COMMUNITY" -> Ide.PYCHARM_COMMUNITY
        "RD", "RIDER" -> Ide.RIDER
        else -> Ide.IDEA_COMMUNITY
    }
}

private fun parseBuildType(type: String?): Ide.BuildType {
    return when (type?.lowercase()) {
        "release" -> Ide.BuildType.RELEASE
        "eap" -> Ide.BuildType.EAP
        else -> Ide.BuildType.EAP
    }
}
