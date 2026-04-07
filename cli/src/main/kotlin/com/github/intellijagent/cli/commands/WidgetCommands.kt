package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.WidgetTools

class ButtonCommand : CliktCommand(name = "button") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the button")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WidgetTools.button(manager.getRemoteRobot(), xpath)
        echo(OutputFormatter.format(mapOf("status" to result), config.json))
    }
}

class CheckboxCommand : CliktCommand(name = "checkbox") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the checkbox")
    private val action by option("--action", help = "Action: check, uncheck, toggle").default("toggle")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WidgetTools.checkbox(manager.getRemoteRobot(), xpath, action)
        echo(OutputFormatter.format(mapOf("checked" to result), config.json))
    }
}

class ComboboxCommand : CliktCommand(name = "combobox") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the combobox")
    private val item by option("--item", help = "Item to select")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WidgetTools.combobox(manager.getRemoteRobot(), xpath, item)
        echo(OutputFormatter.format(mapOf("selected" to result), config.json))
    }
}

class TextboxCommand : CliktCommand(name = "textbox") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the text field")
    private val text by option("--text", help = "Text to set")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WidgetTools.textbox(manager.getRemoteRobot(), xpath, text)
        echo(OutputFormatter.format(mapOf("text" to result), config.json))
    }
}

class TreeCommand : CliktCommand(name = "tree") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the tree")
    private val action by option("--action", help = "Action: expand, collapse, select, paths").default("paths")
    private val path by option("--path", help = "Tree path (comma-separated node names)")

    override fun run() {
        val manager = config.createConnectionManager()
        val pathList = path?.split(",")?.map { it.trim() }
        val result = WidgetTools.tree(manager.getRemoteRobot(), xpath, action, pathList)
        echo(OutputFormatter.format(result, config.json))
    }
}

class TableCommand : CliktCommand(name = "table") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the table")
    private val action by option("--action", help = "Action: read, click").default("read")
    private val row by option("--row", help = "Row index").int()
    private val col by option("--col", help = "Column index").int()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WidgetTools.table(manager.getRemoteRobot(), xpath, action, row, col)
        echo(OutputFormatter.format(result, config.json))
    }
}
