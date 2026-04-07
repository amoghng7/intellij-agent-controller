package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.ComponentTools

class FindAllCommand : CliktCommand(name = "find-all") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = ComponentTools.findAll(manager.getRemoteRobot())
        echo(OutputFormatter.format(result, config.json))
    }
}

class FindCommand : CliktCommand(name = "find") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = ComponentTools.find(manager.getRemoteRobot(), xpath)
        echo(OutputFormatter.format(result, config.json))
    }
}

class ComponentExistsCommand : CliktCommand(name = "component-exists") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = ComponentTools.componentExists(manager.getRemoteRobot(), xpath)
        echo(OutputFormatter.format(result, config.json))
    }
}

class HasTextCommand : CliktCommand(name = "has-text") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")
    private val text by argument(help = "Text to search for")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = ComponentTools.hasText(manager.getRemoteRobot(), xpath, text)
        echo(OutputFormatter.format(result, config.json))
    }
}

class GetPropertyCommand : CliktCommand(name = "get-property") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component")
    private val property by argument(help = "Property name")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = ComponentTools.getProperty(manager.getRemoteRobot(), xpath, property)
        echo(OutputFormatter.format(result, config.json))
    }
}
