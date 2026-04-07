package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.tools.DialogTools

class FindDialogCommand : CliktCommand(name = "find-dialog") {
    private val config by requireObject<CliConfig>()
    private val title by option("--title", help = "Dialog title to search for")

    override fun run() {
        val manager = config.createConnectionManager()
        val result = DialogTools.findDialog(manager.getRemoteRobot(), title)
        echo(OutputFormatter.format(result, config.json))
    }
}

class CloseDialogCommand : CliktCommand(name = "close-dialog") {
    private val config by requireObject<CliConfig>()
    private val action by option("--action", help = "Close action: ok, cancel, close, yes, no").default("close")

    override fun run() {
        val manager = config.createConnectionManager()
        DialogTools.closeDialog(manager.getRemoteRobot(), action)
        echo(OutputFormatter.format(mapOf("status" to "closed", "action" to action), config.json))
    }
}

class BalloonNotificationCommand : CliktCommand(name = "balloon-notification") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val manager = config.createConnectionManager()
        val result = DialogTools.balloonNotification(manager.getRemoteRobot())
        echo(OutputFormatter.format(result, config.json))
    }
}
