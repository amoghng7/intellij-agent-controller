package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComboBoxFixture
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JCheckboxFixture
import com.intellij.remoterobot.fixtures.JTableFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.github.intellijagent.core.models.TreeNodeInfo
import com.github.intellijagent.core.models.TableData

object WidgetTools {

    fun button(robot: RemoteRobot, xpath: String): String {
        val btn = robot.find(JButtonFixture::class.java, byXpath(xpath))
        btn.click()
        return "clicked"
    }

    fun checkbox(robot: RemoteRobot, xpath: String, action: String = "toggle"): Boolean {
        val cb = robot.find(JCheckboxFixture::class.java, byXpath(xpath))
        when (action.lowercase()) {
            "check", "select" -> if (!cb.isSelected()) cb.setValue(true)
            "uncheck", "unselect", "deselect" -> if (cb.isSelected()) cb.setValue(false)
            "toggle" -> cb.setValue(!cb.isSelected())
        }
        return cb.isSelected()
    }

    fun combobox(robot: RemoteRobot, xpath: String, item: String? = null): String {
        val cb = robot.find(ComboBoxFixture::class.java, byXpath(xpath))
        if (item != null) {
            cb.selectItem(item)
        }
        return cb.selectedText()
    }

    fun textbox(robot: RemoteRobot, xpath: String, text: String? = null): String {
        val tf = robot.find(JTextFieldFixture::class.java, byXpath(xpath))
        if (text != null) {
            tf.text = text
        }
        return tf.text
    }

    fun tree(robot: RemoteRobot, xpath: String, action: String, path: List<String>? = null): Any {
        val tree = robot.find(JTreeFixture::class.java, byXpath(xpath))
        return when (action.lowercase()) {
            "expand" -> {
                if (path != null) {
                    tree.expand(*path.toTypedArray())
                } else {
                    tree.expandAll()
                }
                "expanded"
            }
            "collapse" -> {
                if (path != null) {
                    tree.collapsePath(*path.toTypedArray())
                }
                "collapsed"
            }
            "select" -> {
                if (path != null) {
                    tree.clickPath(*path.toTypedArray())
                }
                "selected"
            }
            "paths", "list" -> {
                tree.collectExpandedPaths().map { TreeNodeInfo(it.path) }
            }
            "selected-paths", "selected" -> {
                tree.collectSelectedPaths().map { TreeNodeInfo(it) }
            }
            else -> throw IllegalArgumentException(
                "Unknown tree action: $action. Use: expand, collapse, select, paths, selected-paths"
            )
        }
    }

    fun table(robot: RemoteRobot, xpath: String, action: String, row: Int? = null, col: Int? = null): Any {
        val tbl = robot.find(JTableFixture::class.java, byXpath(xpath))
        return when (action.lowercase()) {
            "read", "data" -> {
                val items = tbl.collectItems()
                TableData(
                    rows = items,
                    rowCount = items.size,
                    columnCount = if (items.isNotEmpty()) items[0].size else 0
                )
            }
            "click", "select" -> {
                requireNotNull(row) { "row is required for click action" }
                requireNotNull(col) { "col is required for click action" }
                tbl.clickCell(row, col)
                "clicked cell ($row, $col)"
            }
            else -> throw IllegalArgumentException("Unknown table action: $action. Use: read, click")
        }
    }
}
