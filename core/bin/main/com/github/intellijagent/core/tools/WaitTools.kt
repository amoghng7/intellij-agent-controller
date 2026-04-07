package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import com.github.intellijagent.core.TimeoutException
import java.time.Duration

object WaitTools {

    fun waitForComponent(
        robot: RemoteRobot,
        xpath: String,
        timeoutMs: Long = 10000,
        condition: String? = null
    ): Boolean {
        try {
            waitFor(Duration.ofMillis(timeoutMs), Duration.ofMillis(500)) {
                try {
                    val fixtures = robot.findAll(ComponentFixture::class.java, byXpath(xpath))
                    if (fixtures.isEmpty()) return@waitFor false
                    if (condition != null) {
                        when (condition.lowercase()) {
                            "visible" -> fixtures.first().callJs<Boolean>("component.isVisible()")
                            "enabled" -> fixtures.first().callJs<Boolean>("component.isEnabled()")
                            "exists" -> true
                            else -> true
                        }
                    } else {
                        true
                    }
                } catch (_: Exception) {
                    false
                }
            }
            return true
        } catch (e: Exception) {
            throw TimeoutException(
                "Timed out waiting for component '$xpath' (timeout: ${timeoutMs}ms)", e
            )
        }
    }

    fun waitForCondition(
        robot: RemoteRobot,
        jsCondition: String,
        timeoutMs: Long = 10000
    ): Boolean {
        try {
            waitFor(Duration.ofMillis(timeoutMs), Duration.ofMillis(500)) {
                try {
                    robot.callJs<Boolean>(jsCondition)
                } catch (_: Exception) {
                    false
                }
            }
            return true
        } catch (e: Exception) {
            throw TimeoutException(
                "Timed out waiting for condition (timeout: ${timeoutMs}ms)", e
            )
        }
    }

    fun smartWait(robot: RemoteRobot, timeoutMs: Long = 60000): Boolean {
        try {
            waitFor(Duration.ofMillis(timeoutMs), Duration.ofMillis(1000)) {
                try {
                    robot.callJs<Boolean>(
                        """
                        importPackage(com.intellij.openapi.project)
                        const projects = ProjectManager.getInstance().getOpenProjects()
                        if (projects.length == 0) { false }
                        else { !com.intellij.openapi.project.DumbService.isDumb(projects[0]) }
                        """.trimIndent(), true
                    )
                } catch (_: Exception) {
                    false
                }
            }
            return true
        } catch (e: Exception) {
            throw TimeoutException(
                "Timed out waiting for IDE to be ready (timeout: ${timeoutMs}ms)", e
            )
        }
    }
}
