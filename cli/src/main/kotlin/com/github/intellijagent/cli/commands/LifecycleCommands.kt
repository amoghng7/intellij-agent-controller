package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.cli.OutputFormatter
import com.github.intellijagent.cli.createConnectionManager
import com.github.intellijagent.core.ConnectionInfo
import com.github.intellijagent.core.ConnectionManager
import com.github.intellijagent.core.IdeConfig
import com.github.intellijagent.core.IdeLifecycleManager
import com.intellij.remoterobot.launcher.Ide
import java.nio.file.Paths

private val connectionFile = Paths.get(
    System.getProperty("user.home"), ".intellij-agent-controller", "connection.json"
)

class LaunchCommand : CliktCommand(name = "launch") {
    private val config by requireObject<CliConfig>()
    private val ide by option("--ide", help = "IDE type (IC, IU, CL, WS, etc.)").default("IC")
    private val version by option("--version", help = "IDE version")
    private val buildType by option("--build-type", help = "Build type (RELEASE, EAP)").default("RELEASE")
    private val port by option("--port", help = "Robot server port").int().default(8580)
    private val plugins by option("--plugins", help = "Plugin paths").multiple()

    override fun run() {
        val ideType = when (ide.uppercase()) {
            "IC" -> Ide.IDEA_COMMUNITY
            "IU" -> Ide.IDEA_ULTIMATE
            "CL" -> Ide.CLION
            "WS" -> Ide.WEBSTORM
            "RM" -> Ide.RUBY_MINE
            "PY" -> Ide.PYCHARM
            "PC" -> Ide.PYCHARM_COMMUNITY
            "RD" -> Ide.RIDER
            else -> Ide.IDEA_COMMUNITY
        }
        val bt = when (buildType.uppercase()) {
            "EAP" -> Ide.BuildType.EAP
            "RELEASE" -> Ide.BuildType.RELEASE
            else -> Ide.BuildType.RELEASE
        }
        val ideConfig = IdeConfig(
            ideType = ideType,
            buildType = bt,
            version = version,
            robotServerPort = port,
            pluginPaths = plugins
        )
        val manager = IdeLifecycleManager(ideConfig)
        manager.launch()

        val info = ConnectionInfo("127.0.0.1", port)
        ConnectionManager.saveConnectionInfo(info, connectionFile)

        echo(OutputFormatter.format(mapOf("status" to "launched", "port" to port), config.json))
    }
}

class ConnectCommand : CliktCommand(name = "connect") {
    private val config by requireObject<CliConfig>()
    private val host by option("--host", help = "Robot server host").default("127.0.0.1")
    private val port by option("--port", help = "Robot server port").int().default(8580)

    override fun run() {
        val manager = ConnectionManager()
        manager.connect(host, port)
        val healthy = manager.checkHealth()

        val info = ConnectionInfo(host, port)
        ConnectionManager.saveConnectionInfo(info, connectionFile)

        echo(OutputFormatter.format(
            mapOf("status" to if (healthy) "connected" else "unreachable", "host" to host, "port" to port),
            config.json
        ))
    }
}

class StatusCommand : CliktCommand(name = "status") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val saved = ConnectionManager.loadConnectionInfo(connectionFile)
        if (saved == null) {
            echo(OutputFormatter.format(mapOf("status" to "not connected"), config.json))
            return
        }
        val manager = ConnectionManager()
        manager.connect(saved.host, saved.port)
        val healthy = manager.checkHealth()
        echo(OutputFormatter.format(
            mapOf("status" to if (healthy) "connected" else "unreachable", "host" to saved.host, "port" to saved.port),
            config.json
        ))
    }
}

class ShutdownCommand : CliktCommand(name = "shutdown") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val ideConfig = IdeConfig(robotServerPort = config.port)
        val manager = IdeLifecycleManager(ideConfig)
        manager.shutdown()
        echo(OutputFormatter.format(mapOf("status" to "shutdown"), config.json))
    }
}

class RestartCommand : CliktCommand(name = "restart") {
    private val config by requireObject<CliConfig>()

    override fun run() {
        val ideConfig = IdeConfig(robotServerPort = config.port)
        val manager = IdeLifecycleManager(ideConfig)
        manager.restart()
        echo(OutputFormatter.format(mapOf("status" to "restarted"), config.json))
    }
}
