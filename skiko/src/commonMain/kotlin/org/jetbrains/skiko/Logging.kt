package org.jetbrains.skiko

object Logger {
    fun info(msg: String) {
        println("[SKIKO] info: $msg")
    }

    fun warn(msg: String) {
        println("[SKIKO] warn: $msg")
    }

    fun error(msg: String) {
        println("[SKIKO] error: $msg")
    }
}
