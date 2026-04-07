package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.JsTools

class ExecuteJsCommand : CliktCommand(name = "execute-js") {
    private val config by requireObject<CliConfig>()
    private val script by argument(help = "JavaScript code to execute")
    private val component by option("--component", help = "Component XPath to scope execution")
    private val edt by option("--edt", help = "Run on EDT thread").flag()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = JsTools.executeJs(manager.getRemoteRobot(), script, component, edt)
        echo(OutputFormatter.format(result ?: "null", config.json))
    }
}

class RunJsCommand : CliktCommand(name = "run-js") {
    private val config by requireObject<CliConfig>()
    private val script by argument(help = "JavaScript code to run (no return value)")
    private val component by option("--component", help = "Component XPath to scope execution")
    private val edt by option("--edt", help = "Run on EDT thread").flag()

    override fun run() {
        val manager = config.createConnectionManager()
        JsTools.runJs(manager.getRemoteRobot(), script, component, edt)
        echo(OutputFormatter.format(mapOf("status" to "ok"), config.json))
    }
}
