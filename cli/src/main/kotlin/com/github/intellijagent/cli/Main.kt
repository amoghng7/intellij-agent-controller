package com.github.intellijagent.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.intellijagent.cli.commands.*
import com.github.intellijagent.core.ConnectionManager
import com.google.gson.GsonBuilder

data class CliConfig(
    val host: String,
    val port: Int,
    val json: Boolean,
    val debug: Boolean
)

fun CliConfig.createConnectionManager(): ConnectionManager {
    val manager = ConnectionManager()
    manager.connect(host, port)
    return manager
}

object OutputFormatter {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun format(data: Any?, json: Boolean): String {
        return if (json) gson.toJson(data) else data.toString()
    }
}

class IjAgent : CliktCommand(name = "ij") {
    private val host by option("--host", help = "Robot server host").default("127.0.0.1")
    private val port by option("--port", help = "Robot server port").int().default(8580)
    private val json by option("--json", help = "Output as JSON").flag()
    private val debug by option("--debug", help = "Enable debug output").flag()

    override fun help(context: Context) = "IntelliJ Agent Controller — control JetBrains IDEs from the command line"

    override fun run() {
        currentContext.findOrSetObject { CliConfig(host, port, json, debug) }
    }
}

fun main(args: Array<String>) {
    IjAgent()
        .subcommands(
            // Lifecycle
            LaunchCommand(),
            ConnectCommand(),
            StatusCommand(),
            ShutdownCommand(),
            RestartCommand(),
            // Components
            FindAllCommand(),
            FindCommand(),
            ComponentExistsCommand(),
            HasTextCommand(),
            GetPropertyCommand(),
            // JavaScript
            ExecuteJsCommand(),
            RunJsCommand(),
            // Interaction
            ClickCommand(),
            DoubleClickCommand(),
            RightClickCommand(),
            TypeCommand(),
            PressKeyCommand(),
            SelectCommand(),
            // Widgets
            ButtonCommand(),
            CheckboxCommand(),
            ComboboxCommand(),
            TextboxCommand(),
            TreeCommand(),
            TableCommand(),
            // Editor
            OpenFileCommand(),
            GetEditorTextCommand(),
            SetEditorTextCommand(),
            EditorInfoCommand(),
            EditorActionCommand(),
            // IDE
            InvokeActionCommand(),
            OpenProjectCommand(),
            CloseProjectCommand(),
            GetProjectPathCommand(),
            NavigateCommand(),
            // JCEF
            JcefFindBrowserCommand(),
            JcefExecuteJsCommand(),
            JcefGetUrlCommand(),
            // Dialog
            FindDialogCommand(),
            CloseDialogCommand(),
            BalloonNotificationCommand(),
            // Diagnostic
            ScreenshotCommand(),
            GetLogsCommand(),
            GetHierarchyCommand(),
            // Wait
            WaitForCommand(),
            WaitForConditionCommand(),
            SmartWaitCommand(),
            // MCP Server
            McpServerCommand(),
        )
        .main(args)
}
