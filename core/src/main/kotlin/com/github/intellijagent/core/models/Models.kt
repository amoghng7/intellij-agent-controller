package com.github.intellijagent.core.models

data class ComponentInfo(
    val id: String,
    val className: String,
    val text: List<String>,
    val visible: Boolean,
    val enabled: Boolean,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class ScreenshotResult(
    val outputPath: String?,
    val base64: String?,
    val width: Int,
    val height: Int
)

data class EditorInfo(
    val fileName: String,
    val filePath: String,
    val text: String,
    val caretOffset: Int,
    val selectedText: String
)

data class DialogInfo(
    val title: String,
    val componentId: String,
    val isModal: Boolean
)

data class NotificationInfo(
    val text: String,
    val type: String
)

data class TreeNodeInfo(
    val path: List<String>
)

data class TableData(
    val rows: List<List<String>>,
    val rowCount: Int,
    val columnCount: Int
)

data class JcefBrowserInfo(
    val componentId: String,
    val url: String?
)
