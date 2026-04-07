package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.option
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.EditorTools
import java.io.File

class OpenFileCommand : CliktCommand(name = "open-file") {
    private val config by requireObject<CliConfig>()
    private val filePath by argument(help = "Path to the file to open")

    override fun run() {
        val manager = config.createConnectionManager()
        EditorTools.openFile(manager.getRemoteRobot(), filePath)
        echo(OutputFormatter.format(mapOf("status" to "opened", "file" to filePath), config.json))
    }
}

class GetEditorTextCommand : CliktCommand(name = "get-editor-text") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = EditorTools.getEditorText(manager.getRemoteRobot())
        echo(OutputFormatter.format(if (config.json) mapOf("text" to result) else result, config.json))
    }
}

class SetEditorTextCommand : CliktCommand(name = "set-editor-text") {
    private val config by requireObject<CliConfig>()
    private val text by argument(help = "Text to set in the editor").optional()
    private val file by option("--file", help = "Read text from file")

    override fun run() {
        val manager = config.createConnectionManager()
        val content = when {
            file != null -> File(file!!).readText()
            text != null -> text!!
            else -> {
                echo("Error: provide either a text argument or --file option", err = true)
                return
            }
        }
        EditorTools.setEditorText(manager.getRemoteRobot(), content)
        echo(OutputFormatter.format(mapOf("status" to "text set"), config.json))
    }
}

class EditorInfoCommand : CliktCommand(name = "editor-info") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = EditorTools.getEditorInfo(manager.getRemoteRobot())
        echo(OutputFormatter.format(result, config.json))
    }
}

class EditorActionCommand : CliktCommand(name = "editor-action") {
    private val config by requireObject<CliConfig>()
    private val actionId by argument(help = "Editor action ID")

    override fun run() {
        val manager = config.createConnectionManager()
        EditorTools.editorAction(manager.getRemoteRobot(), actionId)
        echo(OutputFormatter.format(mapOf("status" to "executed", "action" to actionId), config.json))
    }
}
