package com.github.intellijagent.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.intellijagent.cli.CliConfig
import com.github.intellijagent.mcp.startMcpServer

class McpServerCommand : CliktCommand(name = "mcp-server") {
    private val config by requireObject<CliConfig>()
    private val transport by option("--transport", help = "Transport type (stdio or sse)")
        .choice("stdio", "sse")
        .default("stdio")
    private val mcpPort by option("--mcp-port", help = "MCP server port (for SSE mode)")
        .int()
        .default(3000)

    override fun run() {
        startMcpServer(
            transport = transport,
            mcpPort = mcpPort,
            robotHost = config.host,
            robotPort = config.port
        )
    }
}
