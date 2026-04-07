package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.DiagnosticTools

class ScreenshotCommand : CliktCommand(name = "screenshot") {
    private val config by requireObject<CliConfig>()
    private val component by option("--component", help = "Component XPath for targeted screenshot")
    private val output by option("--output", help = "Output file path")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = DiagnosticTools.screenshot(manager.getRemoteRobot(), component, output)
        echo(OutputFormatter.format(result, config.json))
    }
}

class GetLogsCommand : CliktCommand(name = "get-logs") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val result = DiagnosticTools.getLogs(null)
        echo(OutputFormatter.format(if (config.json) mapOf("logs" to result) else result, config.json))
    }
}

class GetHierarchyCommand : CliktCommand(name = "get-hierarchy") {
    private val config by requireObject<CliConfig>()
    private val format by option("--format", help = "Output format: json, html").default("html")

    override fun run() {
        val url = "http://${config.host}:${config.port}"
        val result = DiagnosticTools.getHierarchyDump(url, format)
        echo(OutputFormatter.format(if (config.json) mapOf("hierarchy" to result) else result, config.json))
    }
}
