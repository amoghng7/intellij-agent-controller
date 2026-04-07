package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.JCefBrowserFixture
import com.github.intellijagent.core.ComponentNotFoundException
import com.github.intellijagent.core.models.JcefBrowserInfo

object JcefTools {

    private fun findBrowserFixture(robot: RemoteRobot): JCefBrowserFixture {
        try {
            return robot.find(JCefBrowserFixture::class.java, JCefBrowserFixture.canvasLocator)
        } catch (e: Exception) {
            try {
                return robot.find(JCefBrowserFixture::class.java, JCefBrowserFixture.macLocator)
            } catch (e2: Exception) {
                throw ComponentNotFoundException("No JCEF browser component found", e2)
            }
        }
    }

    fun jcefFindBrowser(robot: RemoteRobot): JcefBrowserInfo {
        val browser = findBrowserFixture(robot)
        val url = try {
            browser.callJs<String>("local.get('__cefBrowser').getURL()")
        } catch (_: Exception) {
            null
        }
        return JcefBrowserInfo(
            componentId = browser.remoteComponent.id,
            url = url
        )
    }

    fun jcefExecuteJs(robot: RemoteRobot, script: String): String {
        val browser = findBrowserFixture(robot)
        return browser.executeJsInBrowser(script)
    }

    fun jcefGetUrl(robot: RemoteRobot): String {
        val browser = findBrowserFixture(robot)
        return browser.callJs("local.get('__cefBrowser').getURL()")
    }
}
