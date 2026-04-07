package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.github.intellijagent.core.ExecutionException

object JsTools {
    fun executeJs(
        robot: RemoteRobot,
        script: String,
        componentXpath: String? = null,
        runInEdt: Boolean = false
    ): String? {
        try {
            return if (componentXpath != null) {
                val fixture = ComponentTools.findFixture(robot, componentXpath)
                fixture.callJs<String>(script, runInEdt)
            } else {
                robot.callJs<String>(script, runInEdt)
            }
        } catch (e: Exception) {
            throw ExecutionException("JavaScript execution failed: ${e.message}", e)
        }
    }

    fun runJs(
        robot: RemoteRobot,
        script: String,
        componentXpath: String? = null,
        runInEdt: Boolean = false
    ) {
        try {
            if (componentXpath != null) {
                val fixture = ComponentTools.findFixture(robot, componentXpath)
                fixture.runJs(script, runInEdt)
            } else {
                robot.runJs(script, runInEdt)
            }
        } catch (e: Exception) {
            throw ExecutionException("JavaScript execution failed: ${e.message}", e)
        }
    }
}
