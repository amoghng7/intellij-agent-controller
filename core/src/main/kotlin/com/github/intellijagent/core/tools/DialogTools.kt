package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.github.intellijagent.core.ComponentNotFoundException
import com.github.intellijagent.core.models.DialogInfo
import com.github.intellijagent.core.models.NotificationInfo

object DialogTools {

    fun findDialog(robot: RemoteRobot, title: String? = null): DialogInfo {
        val xpath = if (title != null) {
            "//div[@class='MyDialog' and @title='$title']"
        } else {
            "//div[@class='MyDialog']"
        }
        val fixture = try {
            robot.find(ComponentFixture::class.java, byXpath(xpath))
        } catch (e: Exception) {
            val fallbackXpath = if (title != null) {
                "//div[@class='JDialog' and @title='$title']"
            } else {
                "//div[@class='JDialog']"
            }
            try {
                robot.find(ComponentFixture::class.java, byXpath(fallbackXpath))
            } catch (e2: Exception) {
                try {
                    robot.find(
                        ComponentFixture::class.java,
                        byXpath("//div[@class='DialogWrapperPeerImpl']")
                    )
                } catch (e3: Exception) {
                    throw ComponentNotFoundException(
                        "No dialog found${title?.let { " with title '$it'" } ?: ""}",
                        e3
                    )
                }
            }
        }
        val dialogTitle = try {
            fixture.callJs<String>("component.getTitle()")
        } catch (_: Exception) {
            title ?: "Unknown"
        }
        return DialogInfo(
            title = dialogTitle,
            componentId = fixture.remoteComponent.id,
            isModal = try {
                fixture.callJs("component.isModal()")
            } catch (_: Exception) {
                false
            }
        )
    }

    fun closeDialog(robot: RemoteRobot, action: String = "close") {
        val buttonText = when (action.lowercase()) {
            "ok" -> "OK"
            "cancel" -> "Cancel"
            "close" -> "Close"
            "yes" -> "Yes"
            "no" -> "No"
            else -> action
        }
        try {
            val button = robot.find(
                ComponentFixture::class.java,
                byXpath("//div[@class='JButton' and @text='$buttonText']")
            )
            button.click()
        } catch (e: Exception) {
            if (action.lowercase() in listOf("cancel", "close")) {
                InteractionTools.pressKey(robot, "escape")
            } else {
                throw ComponentNotFoundException("Dialog button '$buttonText' not found", e)
            }
        }
    }

    fun balloonNotification(robot: RemoteRobot): List<NotificationInfo> {
        val notifications = mutableListOf<NotificationInfo>()
        val xpaths = listOf(
            "//div[@class='BalloonImpl']",
            "//div[@class='NotificationBalloon']",
            "//div[@class='StatusBarPanel']//div[@class='WithIconAndArrows']"
        )
        for (xpath in xpaths) {
            try {
                val fixtures = robot.findAll(ComponentFixture::class.java, byXpath(xpath))
                for (fixture in fixtures) {
                    val texts = fixture.findAllText().map { it.text }
                    notifications.add(
                        NotificationInfo(
                            text = texts.joinToString(" "),
                            type = "balloon"
                        )
                    )
                }
            } catch (_: Exception) {
                // Skip if not found
            }
        }
        return notifications
    }
}
