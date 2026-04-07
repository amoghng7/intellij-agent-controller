package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.github.intellijagent.core.models.ScreenshotResult
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.util.Base64
import javax.imageio.ImageIO

object DiagnosticTools {

    fun screenshot(
        robot: RemoteRobot,
        componentXpath: String? = null,
        outputPath: String? = null
    ): ScreenshotResult {
        val image = if (componentXpath != null) {
            val fixture = ComponentTools.findFixture(robot, componentXpath)
            fixture.getScreenshot()
        } else {
            robot.getScreenshot()
        }

        val savedPath = if (outputPath != null) {
            val file = File(outputPath)
            file.parentFile?.mkdirs()
            ImageIO.write(image, "png", file)
            file.absolutePath
        } else {
            null
        }

        val base64 = if (outputPath == null) {
            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "png", baos)
            Base64.getEncoder().encodeToString(baos.toByteArray())
        } else {
            null
        }

        return ScreenshotResult(
            outputPath = savedPath,
            base64 = base64,
            width = image.width,
            height = image.height
        )
    }

    fun getLogs(logDirectory: Path?): String {
        if (logDirectory == null) {
            return "No log directory available. IDE may not have been launched via this tool."
        }
        val logFile = logDirectory.resolve("idea.log").toFile()
        return if (logFile.exists()) {
            val lines = logFile.readLines()
            val start = maxOf(0, lines.size - 500)
            lines.subList(start, lines.size).joinToString("\n")
        } else {
            "Log file not found at: ${logFile.absolutePath}"
        }
    }

    fun getHierarchyDump(robotServerUrl: String, format: String = "html"): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(robotServerUrl).build()
        val response = client.newCall(request).execute()
        val html = response.body?.string() ?: ""

        return when (format.lowercase()) {
            "json" -> convertHierarchyToJson(html)
            "html", "xml" -> html
            else -> html
        }
    }

    private fun convertHierarchyToJson(html: String): String {
        val components = mutableListOf<Map<String, String>>()
        val regex = Regex("""class="([^"]*)"[^>]*?(?:text="([^"]*)")?""")
        for (match in regex.findAll(html)) {
            components.add(
                mapOf(
                    "class" to match.groupValues[1],
                    "text" to (match.groupValues.getOrNull(2) ?: "")
                )
            )
        }
        val gson = com.google.gson.Gson()
        return gson.toJson(components)
    }
}
