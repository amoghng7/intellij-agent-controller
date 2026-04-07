package com.github.intellijagent.core

import com.intellij.remoterobot.launcher.Ide

data class IdeConfig(
    val ideType: Ide = Ide.IDEA_COMMUNITY,
    val buildType: Ide.BuildType = Ide.BuildType.EAP,
    val version: String? = null,
    val robotServerPort: Int = 8580,
    val additionalProperties: Map<String, Any> = emptyMap(),
    val additionalVmOptions: List<String> = emptyList(),
    val pluginPaths: List<String> = emptyList()
)
