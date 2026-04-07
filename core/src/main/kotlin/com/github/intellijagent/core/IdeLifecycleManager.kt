package com.github.intellijagent.core

import com.intellij.remoterobot.launcher.IdeDownloader
import com.intellij.remoterobot.launcher.IdeLauncher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

data class IdeStatus(val processAlive: Boolean, val robotServerReady: Boolean)

class IdeLifecycleManager(
    private val config: IdeConfig,
    private val cacheDir: Path = Paths.get(System.getProperty("user.home"), ".intellij-agent-controller")
) {
    private var process: Process? = null
    private var sandboxDir: Path? = null

    private val idesCacheDir: Path get() = cacheDir.resolve("ides")
    private val pluginsCacheDir: Path get() = cacheDir.resolve("plugins")
    private val sandboxCacheDir: Path get() = cacheDir.resolve("sandbox")

    fun launch() {
        Files.createDirectories(idesCacheDir)
        Files.createDirectories(pluginsCacheDir)
        Files.createDirectories(sandboxCacheDir)

        val httpClient = OkHttpClient()
        val ideDownloader = IdeDownloader(httpClient)

        val idePath = ideDownloader.downloadAndExtract(
            config.ideType,
            idesCacheDir,
            config.buildType,
            config.version
        )

        val robotPluginPath = ideDownloader.downloadRobotPlugin(pluginsCacheDir)

        val extraPlugins = config.pluginPaths.map { Paths.get(it) }
        val allPlugins = listOf(robotPluginPath) + extraPlugins

        val properties = mapOf<String, Any>(
            "robot-server.port" to config.robotServerPort,
            "jb.consents.confirmation.enabled" to false,
            "idea.trust.all.projects" to true,
            "ide.show.tips.on.startup.default.value" to false
        ) + config.additionalProperties

        val sandbox = Files.createTempDirectory(sandboxCacheDir, "sandbox-")
        sandboxDir = sandbox

        process = IdeLauncher.launchIde(
            idePath,
            properties,
            config.additionalVmOptions,
            allPlugins,
            sandbox
        )

        waitForRobotServer(httpClient)
    }

    fun shutdown() {
        val proc = process ?: return
        proc.destroy()
        if (!proc.waitFor(10, TimeUnit.SECONDS)) {
            proc.destroyForcibly()
        }
        process = null
    }

    fun restart() {
        shutdown()
        launch()
    }

    fun getStatus(): IdeStatus {
        val proc = process
        val alive = proc?.isAlive ?: false
        val ready = if (alive) {
            checkRobotServer()
        } else {
            false
        }
        return IdeStatus(processAlive = alive, robotServerReady = ready)
    }

    fun getLogDirectory(): Path? {
        val sandbox = sandboxDir ?: return null
        return Files.list(sandbox)
            .filter { it.fileName.toString().startsWith("log") }
            .findFirst()
            .orElse(null)
    }

    private fun waitForRobotServer(httpClient: OkHttpClient) {
        val url = "http://127.0.0.1:${config.robotServerPort}"
        val deadline = System.currentTimeMillis() + 120_000
        while (System.currentTimeMillis() < deadline) {
            if (checkUrl(httpClient, url)) return
            Thread.sleep(2_000)
        }
        throw TimeoutException("Robot server did not become available within 120 seconds at $url")
    }

    private fun checkRobotServer(): Boolean {
        return checkUrl(OkHttpClient(), "http://127.0.0.1:${config.robotServerPort}")
    }

    private fun checkUrl(client: OkHttpClient, url: String): Boolean {
        return runCatching {
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            response.close()
            response.isSuccessful
        }.getOrDefault(false)
    }
}
