plugins {
    application
    id("com.gradleup.shadow")
}

application {
    mainClass.set("com.github.intellijagent.cli.MainKt")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":mcp-server"))
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.shadowJar {
    archiveBaseName.set("intellij-agent-controller")
    archiveClassifier.set("")
    archiveVersion.set("")
}
