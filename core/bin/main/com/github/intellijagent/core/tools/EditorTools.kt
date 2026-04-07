package com.github.intellijagent.core.tools

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.github.intellijagent.core.models.EditorInfo

object EditorTools {

    private const val EDITOR_XPATH = "//div[@class='EditorComponentImpl']"

    fun openFile(robot: RemoteRobot, filePath: String) {
        val escaped = filePath.replace("\\", "/").replace("\"", "\\\"")
        robot.runJs(
            """
            importPackage(com.intellij.openapi.application)
            importPackage(com.intellij.openapi.fileEditor)
            importPackage(com.intellij.openapi.vfs)
            importPackage(com.intellij.openapi.project)
            
            const project = com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0]
            const virtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath("$escaped")
            if (virtualFile == null) {
                throw new Error("File not found: $escaped")
            }
            ApplicationManager.getApplication().invokeLater(new Runnable({
                run: function() {
                    FileEditorManager.getInstance(project).openFile(virtualFile, true)
                }
            }))
            """.trimIndent(), true
        )
    }

    fun getEditorText(robot: RemoteRobot): String {
        val editor = robot.find(ComponentFixture::class.java, byXpath(EDITOR_XPATH))
        editor.runJs(
            """
            local.put('editor', component.getEditor())
            local.put('document', component.getEditor().getDocument())
            """.trimIndent()
        )
        return editor.callJs("local.get('document').getText()")
    }

    fun setEditorText(robot: RemoteRobot, text: String) {
        val escaped = text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
        val editor = robot.find(ComponentFixture::class.java, byXpath(EDITOR_XPATH))
        editor.runJs(
            """
            importPackage(com.intellij.openapi.command)
            local.put('editor', component.getEditor())
            local.put('document', component.getEditor().getDocument())
            const editor = local.get('editor')
            const document = local.get('document')
            const project = editor.getProject()
            WriteCommandAction.runWriteCommandAction(project, new Runnable({
                run: function () {
                    document.setText("$escaped")
                }
            }))
            """.trimIndent()
        )
    }

    fun getEditorInfo(robot: RemoteRobot): EditorInfo {
        val editor = robot.find(ComponentFixture::class.java, byXpath(EDITOR_XPATH))
        editor.runJs(
            """
            local.put('editor', component.getEditor())
            local.put('document', component.getEditor().getDocument())
            """.trimIndent()
        )
        val text: String = editor.callJs("local.get('document').getText()")
        val fileName: String = editor.callJs("local.get('editor').getVirtualFile().getName()", true)
        val filePath: String = editor.callJs("local.get('editor').getVirtualFile().getPath()", true)
        val caretOffset: Int = editor.callJs("local.get('editor').getCaretModel().getOffset()", true)
        val selectedText: String = editor.callJs(
            """
            let selectedText = local.get('editor').getSelectionModel().getSelectedText()
            if (!selectedText) { selectedText = "" }
            selectedText
            """.trimIndent()
        )
        return EditorInfo(fileName, filePath, text, caretOffset, selectedText)
    }

    fun editorAction(robot: RemoteRobot, actionId: String) {
        val escapedId = actionId.replace("\"", "\\\"")
        robot.runJs(
            """
            importPackage(com.intellij.openapi.actionSystem)
            const actionManager = ActionManager.getInstance()
            const action = actionManager.getAction("$escapedId")
            if (action == null) {
                throw new Error("Action not found: $escapedId")
            }
            actionManager.tryToExecute(action, com.intellij.openapi.ui.playback.commands.ActionCommand.getInputEvent("$escapedId"), null, null, true)
            """.trimIndent(), true
        )
    }
}
