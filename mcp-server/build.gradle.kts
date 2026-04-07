plugins {
    application
}

application {
    mainClass.set("com.github.intellijagent.mcp.McpServerKt")
}

dependencies {
    implementation(project(":core"))
    implementation("io.modelcontextprotocol:kotlin-sdk:0.7.0")
    implementation("io.ktor:ktor-server-netty:3.1.1")
    implementation("io.ktor:ktor-server-sse:3.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(kotlin("test"))
}
