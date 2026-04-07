package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.github.intellijagent.core.ComponentNotFoundException
import com.github.intellijagent.core.models.ComponentInfo

object ComponentTools {
    fun findAll(robot: RemoteRobot): List<ComponentInfo> {
        val components = robot.findAll(ComponentFixture::class.java, byXpath("//div"))
        return components.map { it.toComponentInfo() }
    }

    fun find(robot: RemoteRobot, xpath: String): ComponentInfo {
        try {
            val fixture = robot.find(ComponentFixture::class.java, byXpath(xpath))
            return fixture.toComponentInfo()
        } catch (e: Exception) {
            throw ComponentNotFoundException("Component not found: $xpath", e)
        }
    }

    fun componentExists(robot: RemoteRobot, xpath: String): Boolean {
        return try {
            robot.findAll(ComponentFixture::class.java, byXpath(xpath)).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun hasText(robot: RemoteRobot, xpath: String, expectedText: String): Boolean {
        val fixture = findFixture(robot, xpath)
        return fixture.findAllText().any { it.text.contains(expectedText) }
    }

    fun getProperty(robot: RemoteRobot, xpath: String, propertyName: String): String {
        val fixture = findFixture(robot, xpath)
        return fixture.callJs<String>(
            "component.get${propertyName.replaceFirstChar { it.uppercase() }}().toString()"
        )
    }

    fun findFixture(robot: RemoteRobot, xpath: String): ComponentFixture {
        try {
            return robot.find(ComponentFixture::class.java, byXpath(xpath))
        } catch (e: Exception) {
            throw ComponentNotFoundException("Component not found: $xpath", e)
        }
    }

    private fun ComponentFixture.toComponentInfo(): ComponentInfo {
        val rc = this.remoteComponent
        val texts = try { this.findAllText().map { it.text } } catch (_: Exception) { emptyList() }
        val visible = try { this.callJs<Boolean>("component.isVisible()") } catch (_: Exception) { true }
        val enabled = try { this.callJs<Boolean>("component.isEnabled()") } catch (_: Exception) { true }
        return ComponentInfo(
            id = rc.id,
            className = rc.className,
            text = texts,
            visible = visible,
            enabled = enabled,
            x = rc.x,
            y = rc.y,
            width = rc.width,
            height = rc.height
        )
    }
}
