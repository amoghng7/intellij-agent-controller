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
import com.github.intellijagent.core.tools.InteractionTools

class ClickCommand : CliktCommand(name = "click") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")
    private val button by option("--button", help = "Mouse button (left/right/middle)").default("left")
    private val times by option("--times", help = "Number of clicks").int().default(1)

    override fun run() {
        val manager = config.createConnectionManager()
        InteractionTools.click(manager.getRemoteRobot(), xpath, button, times)
        echo(OutputFormatter.format(mapOf("status" to "clicked", "xpath" to xpath), config.json))
    }
}

class DoubleClickCommand : CliktCommand(name = "double-click") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")

    override fun run() {
        val manager = config.createConnectionManager()
        InteractionTools.doubleClick(manager.getRemoteRobot(), xpath)
        echo(OutputFormatter.format(mapOf("status" to "double-clicked", "xpath" to xpath), config.json))
    }
}

class RightClickCommand : CliktCommand(name = "right-click") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")

    override fun run() {
        val manager = config.createConnectionManager()
        InteractionTools.rightClick(manager.getRemoteRobot(), xpath)
        echo(OutputFormatter.format(mapOf("status" to "right-clicked", "xpath" to xpath), config.json))
    }
}

class TypeCommand : CliktCommand(name = "type") {
    private val config by requireObject<CliConfig>()
    private val text by argument(help = "Text to type")

    override fun run() {
        val manager = config.createConnectionManager()
        InteractionTools.typeText(manager.getRemoteRobot(), text)
        echo(OutputFormatter.format(mapOf("status" to "typed"), config.json))
    }
}

class PressKeyCommand : CliktCommand(name = "press-key") {
    private val config by requireObject<CliConfig>()
    private val key by argument(help = "Key combination (e.g. ctrl+s, enter, escape)")

    override fun run() {
        val manager = config.createConnectionManager()
        InteractionTools.pressKey(manager.getRemoteRobot(), key)
        echo(OutputFormatter.format(mapOf("status" to "pressed", "key" to key), config.json))
    }
}

class SelectCommand : CliktCommand(name = "select") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")
    private val item by argument(help = "Item to select")

    override fun run() {
        val manager = config.createConnectionManager()
        InteractionTools.select(manager.getRemoteRobot(), xpath, item)
        echo(OutputFormatter.format(mapOf("status" to "selected", "item" to item), config.json))
    }
}
