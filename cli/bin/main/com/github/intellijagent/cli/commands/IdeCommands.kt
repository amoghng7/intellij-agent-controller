package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.IdeTools

class InvokeActionCommand : CliktCommand(name = "invoke-action") {
    private val config by requireObject<CliConfig>()
    private val actionId by argument(help = "Action ID to invoke")

    override fun run() {
        val manager = config.createConnectionManager()
        IdeTools.invokeAction(manager.getRemoteRobot(), actionId)
        echo(OutputFormatter.format(mapOf("status" to "invoked", "action" to actionId), config.json))
    }
}

class OpenProjectCommand : CliktCommand(name = "open-project") {
    private val config by requireObject<CliConfig>()
    private val projectPath by argument(help = "Path to the project")

    override fun run() {
        val manager = config.createConnectionManager()
        IdeTools.openProject(manager.getRemoteRobot(), projectPath)
        echo(OutputFormatter.format(mapOf("status" to "opened", "path" to projectPath), config.json))
    }
}

class CloseProjectCommand : CliktCommand(name = "close-project") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        IdeTools.closeProject(manager.getRemoteRobot())
        echo(OutputFormatter.format(mapOf("status" to "closed"), config.json))
    }
}

class GetProjectPathCommand : CliktCommand(name = "get-project-path") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = IdeTools.getProjectPath(manager.getRemoteRobot())
        echo(OutputFormatter.format(
            if (result != null) mapOf("path" to result) else mapOf("path" to null),
            config.json
        ))
    }
}

class NavigateCommand : CliktCommand(name = "navigate") {
    private val config by requireObject<CliConfig>()
    private val target by argument(help = "Navigation target")
    private val type by option("--type", help = "Navigation type: file, class, symbol, action").default("file")

    override fun run() {
        val manager = config.createConnectionManager()
        IdeTools.navigate(manager.getRemoteRobot(), target, type)
        echo(OutputFormatter.format(mapOf("status" to "navigated", "target" to target, "type" to type), config.json))
    }
}
