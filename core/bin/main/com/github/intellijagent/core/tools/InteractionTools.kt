package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot

object InteractionTools {

    fun click(robot: RemoteRobot, xpath: String, button: String = "left", times: Int = 1) {
        val fixture = ComponentTools.findFixture(robot, xpath)
        val mouseButton = when (button.lowercase()) {
            "left" -> "MouseButton.LEFT_BUTTON"
            "right" -> "MouseButton.RIGHT_BUTTON"
            "middle" -> "MouseButton.MIDDLE_BUTTON"
            else -> "MouseButton.LEFT_BUTTON"
        }
        fixture.runJs(
            """
            robot.click(component, new Point(component.getWidth() / 2, component.getHeight() / 2), $mouseButton, $times)
            """.trimIndent()
        )
    }

    fun doubleClick(robot: RemoteRobot, xpath: String) {
        click(robot, xpath, "left", 2)
    }

    fun rightClick(robot: RemoteRobot, xpath: String) {
        click(robot, xpath, "right", 1)
    }

    fun typeText(robot: RemoteRobot, text: String) {
        val escapedText = text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
        robot.runJs(
            """
            robot.type("$escapedText")
            """.trimIndent()
        )
    }

    fun pressKey(robot: RemoteRobot, key: String) {
        val keyScript = buildKeyPressScript(key)
        robot.runJs(keyScript)
    }

    fun select(robot: RemoteRobot, xpath: String, item: String) {
        val fixture = ComponentTools.findFixture(robot, xpath)
        val className = fixture.remoteComponent.className
        val escapedItem = item.replace("\\", "\\\\").replace("\"", "\\\"")
        when {
            className.contains("ComboBox") || className.contains("JComboBox") -> {
                fixture.runJs("""component.setSelectedItem("$escapedItem")""")
            }
            className.contains("JList") -> {
                fixture.runJs(
                    """
                    const model = component.getModel()
                    for (let i = 0; i < model.getSize(); i++) {
                        if (model.getElementAt(i).toString() === "$escapedItem") {
                            component.setSelectedIndex(i)
                            break
                        }
                    }
                    """.trimIndent()
                )
            }
            else -> {
                fixture.findText(item).click()
            }
        }
    }

    private fun buildKeyPressScript(key: String): String {
        val parts = key.lowercase().split("+").map { it.trim() }
        val modifiers = mutableListOf<String>()
        var mainKey = ""

        for (part in parts) {
            when (part) {
                "ctrl", "control" -> modifiers.add("KeyEvent.VK_CONTROL")
                "shift" -> modifiers.add("KeyEvent.VK_SHIFT")
                "alt" -> modifiers.add("KeyEvent.VK_ALT")
                "meta", "cmd", "command" -> modifiers.add("KeyEvent.VK_META")
                else -> mainKey = resolveKeyCode(part)
            }
        }

        val sb = StringBuilder()
        sb.appendLine("importClass(java.awt.event.KeyEvent)")
        for (mod in modifiers) {
            sb.appendLine("robot.pressKey($mod)")
        }
        sb.appendLine("robot.pressKey($mainKey)")
        sb.appendLine("robot.releaseKey($mainKey)")
        for (mod in modifiers.reversed()) {
            sb.appendLine("robot.releaseKey($mod)")
        }
        return sb.toString()
    }

    private fun resolveKeyCode(key: String): String {
        return when (key.lowercase()) {
            "enter", "return" -> "KeyEvent.VK_ENTER"
            "escape", "esc" -> "KeyEvent.VK_ESCAPE"
            "tab" -> "KeyEvent.VK_TAB"
            "space" -> "KeyEvent.VK_SPACE"
            "backspace", "back_space" -> "KeyEvent.VK_BACK_SPACE"
            "delete", "del" -> "KeyEvent.VK_DELETE"
            "up" -> "KeyEvent.VK_UP"
            "down" -> "KeyEvent.VK_DOWN"
            "left" -> "KeyEvent.VK_LEFT"
            "right" -> "KeyEvent.VK_RIGHT"
            "home" -> "KeyEvent.VK_HOME"
            "end" -> "KeyEvent.VK_END"
            "page_up", "pageup" -> "KeyEvent.VK_PAGE_UP"
            "page_down", "pagedown" -> "KeyEvent.VK_PAGE_DOWN"
            "f1" -> "KeyEvent.VK_F1"
            "f2" -> "KeyEvent.VK_F2"
            "f3" -> "KeyEvent.VK_F3"
            "f4" -> "KeyEvent.VK_F4"
            "f5" -> "KeyEvent.VK_F5"
            "f6" -> "KeyEvent.VK_F6"
            "f7" -> "KeyEvent.VK_F7"
            "f8" -> "KeyEvent.VK_F8"
            "f9" -> "KeyEvent.VK_F9"
            "f10" -> "KeyEvent.VK_F10"
            "f11" -> "KeyEvent.VK_F11"
            "f12" -> "KeyEvent.VK_F12"
            "a" -> "KeyEvent.VK_A"
            "b" -> "KeyEvent.VK_B"
            "c" -> "KeyEvent.VK_C"
            "d" -> "KeyEvent.VK_D"
            "e" -> "KeyEvent.VK_E"
            "f" -> "KeyEvent.VK_F"
            "g" -> "KeyEvent.VK_G"
            "h" -> "KeyEvent.VK_H"
            "i" -> "KeyEvent.VK_I"
            "j" -> "KeyEvent.VK_J"
            "k" -> "KeyEvent.VK_K"
            "l" -> "KeyEvent.VK_L"
            "m" -> "KeyEvent.VK_M"
            "n" -> "KeyEvent.VK_N"
            "o" -> "KeyEvent.VK_O"
            "p" -> "KeyEvent.VK_P"
            "q" -> "KeyEvent.VK_Q"
            "r" -> "KeyEvent.VK_R"
            "s" -> "KeyEvent.VK_S"
            "t" -> "KeyEvent.VK_T"
            "u" -> "KeyEvent.VK_U"
            "v" -> "KeyEvent.VK_V"
            "w" -> "KeyEvent.VK_W"
            "x" -> "KeyEvent.VK_X"
            "y" -> "KeyEvent.VK_Y"
            "z" -> "KeyEvent.VK_Z"
            else -> "KeyEvent.VK_${key.uppercase()}"
        }
    }
}
