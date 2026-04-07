plugins {
    `java-library`
}

dependencies {
    api("com.intellij.remoterobot:remote-robot:0.11.23")
    api("com.intellij.remoterobot:remote-fixtures:0.11.23")
    api("com.intellij.remoterobot:ide-launcher:0.11.23")
    api("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}
