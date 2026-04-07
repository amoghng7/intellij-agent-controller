package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot

object IdeTools {

    fun invokeAction(robot: RemoteRobot, actionId: String) {
        val escapedId = actionId.replace("\"", "\\\"")
        robot.runJs(
            """
            importPackage(com.intellij.openapi.actionSystem)
            importPackage(com.intellij.openapi.application)
            
            const actionId = "$escapedId"
            const actionManager = ActionManager.getInstance()
            const action = actionManager.getAction(actionId)
            if (action == null) {
                throw new Error("Action not found: " + actionId)
            }
            
            const runAction = new Runnable({
                run: function() {
                    actionManager.tryToExecute(action, com.intellij.openapi.ui.playback.commands.ActionCommand.getInputEvent(actionId), null, null, true)
                }
            })
            ApplicationManager.getApplication().invokeLater(runAction)
            """.trimIndent(), true
        )
    }

    fun openProject(robot: RemoteRobot, projectPath: String) {
        val escaped = projectPath.replace("\\", "/").replace("\"", "\\\"")
        robot.runJs(
            """
            importPackage(com.intellij.openapi.application)
            
            ApplicationManager.getApplication().invokeLater(new Runnable({
                run: function() {
                    com.intellij.ide.impl.ProjectUtil.openOrImport(java.nio.file.Paths.get("$escaped"))
                }
            }))
            """.trimIndent(), true
        )
    }

    fun closeProject(robot: RemoteRobot) {
        robot.runJs(
            """
            importPackage(com.intellij.openapi.project)
            importPackage(com.intellij.openapi.application)
            
            const projects = ProjectManager.getInstance().getOpenProjects()
            if (projects.length > 0) {
                ApplicationManager.getApplication().invokeLater(new Runnable({
                    run: function() {
                        ProjectManager.getInstance().closeAndDispose(projects[0])
                    }
                }))
            }
            """.trimIndent(), true
        )
    }

    fun getProjectPath(robot: RemoteRobot): String? {
        return try {
            robot.callJs<String>(
                """
                importPackage(com.intellij.openapi.project)
                const projects = ProjectManager.getInstance().getOpenProjects()
                if (projects.length > 0) {
                    projects[0].getBasePath()
                } else {
                    ""
                }
                """.trimIndent(), true
            ).ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    fun navigate(robot: RemoteRobot, target: String, type: String = "file") {
        val actionId = when (type.lowercase()) {
            "file" -> "GotoFile"
            "class" -> "GotoClass"
            "symbol" -> "GotoSymbol"
            "action" -> "GotoAction"
            else -> "GotoFile"
        }
        invokeAction(robot, actionId)
        Thread.sleep(500)
        InteractionTools.typeText(robot, target)
    }
}
