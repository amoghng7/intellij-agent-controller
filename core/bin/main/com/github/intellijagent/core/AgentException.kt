package com.github.intellijagent.core

open class AgentException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class ConnectionException(message: String, cause: Throwable? = null) : AgentException(message, cause)
class ComponentNotFoundException(message: String, cause: Throwable? = null) : AgentException(message, cause)
class ExecutionException(message: String, cause: Throwable? = null) : AgentException(message, cause)
class TimeoutException(message: String, cause: Throwable? = null) : AgentException(message, cause)
