package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.JcefTools

class JcefFindBrowserCommand : CliktCommand(name = "jcef-find-browser") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = JcefTools.jcefFindBrowser(manager.getRemoteRobot())
        echo(OutputFormatter.format(result, config.json))
    }
}

class JcefExecuteJsCommand : CliktCommand(name = "jcef-execute-js") {
    private val config by requireObject<CliConfig>()
    private val script by argument(help = "JavaScript code to execute in the JCEF browser")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = JcefTools.jcefExecuteJs(manager.getRemoteRobot(), script)
        echo(OutputFormatter.format(if (config.json) mapOf("result" to result) else result, config.json))
    }
}

class JcefGetUrlCommand : CliktCommand(name = "jcef-get-url") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = JcefTools.jcefGetUrl(manager.getRemoteRobot())
        echo(OutputFormatter.format(if (config.json) mapOf("url" to result) else result, config.json))
    }
}
