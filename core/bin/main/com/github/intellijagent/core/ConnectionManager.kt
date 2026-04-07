package com.github.intellijagent.core

import com.google.gson.Gson
import com.intellij.remoterobot.RemoteRobot
import java.nio.file.Files
import java.nio.file.Path

data class ConnectionInfo(val host: String, val port: Int)

class ConnectionManager {
    private var remoteRobot: RemoteRobot? = null
    private var host: String? = null
    private var port: Int? = null

    fun connect(host: String = "127.0.0.1", port: Int = 8580) {
        this.host = host
        this.port = port
        this.remoteRobot = RemoteRobot("http://$host:$port")
    }

    fun disconnect() {
        remoteRobot = null
        host = null
        port = null
    }

    fun getRemoteRobot(): RemoteRobot {
        return remoteRobot ?: throw ConnectionException("Not connected to any IDE instance")
    }

    fun isConnected(): Boolean = remoteRobot != null

    fun checkHealth(): Boolean {
        val robot = remoteRobot ?: return false
        return runCatching {
            robot.callJs<Boolean>("true")
        }.getOrDefault(false)
    }

    fun getConnectionInfo(): ConnectionInfo {
        val h = host ?: throw ConnectionException("Not connected to any IDE instance")
        val p = port ?: throw ConnectionException("Not connected to any IDE instance")
        return ConnectionInfo(h, p)
    }

    companion object {
        private val gson = Gson()

        fun saveConnectionInfo(info: ConnectionInfo, file: Path) {
            Files.createDirectories(file.parent)
            Files.writeString(file, gson.toJson(info))
        }

        fun loadConnectionInfo(file: Path): ConnectionInfo? {
            if (!Files.exists(file)) return null
            return runCatching {
                gson.fromJson(Files.readString(file), ConnectionInfo::class.java)
            }.getOrNull()
        }
    }
}
