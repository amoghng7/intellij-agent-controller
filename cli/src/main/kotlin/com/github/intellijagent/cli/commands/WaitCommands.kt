package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.WaitTools

class WaitForCommand : CliktCommand(name = "wait-for") {
    private val config by requireObject<CliConfig>()
    private val xpath by argument(help = "XPath selector for the component to wait for")
    private val timeout by option("--timeout", help = "Timeout in milliseconds").long().default(10000L)
    private val condition by option("--condition", help = "Condition: visible, enabled, exists").default("exists")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WaitTools.waitForComponent(manager.getRemoteRobot(), xpath, timeout, condition)
        echo(OutputFormatter.format(mapOf("found" to result, "xpath" to xpath), config.json))
    }
}

class WaitForConditionCommand : CliktCommand(name = "wait-for-condition") {
    private val config by requireObject<CliConfig>()
    private val jsCondition by argument(help = "JavaScript condition expression")
    private val timeout by option("--timeout", help = "Timeout in milliseconds").long().default(10000L)

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WaitTools.waitForCondition(manager.getRemoteRobot(), jsCondition, timeout)
        echo(OutputFormatter.format(mapOf("satisfied" to result), config.json))
    }
}

class SmartWaitCommand : CliktCommand(name = "smart-wait") {
    private val config by requireObject<CliConfig>()
    private val timeout by option("--timeout", help = "Timeout in milliseconds").long().default(60000L)

    override fun run() {
        val manager = config.createConnectionManager()
        val result = WaitTools.smartWait(manager.getRemoteRobot(), timeout)
        echo(OutputFormatter.format(mapOf("ready" to result), config.json))
    }
}
