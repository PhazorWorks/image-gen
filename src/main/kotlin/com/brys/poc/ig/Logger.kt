package com.brys.poc.ig

object Logger {
    private val emojis = listOf("","✅","⛔","\uD83D\uDCA1","⚠️","ℹ️")
    private val colour = listOf("\u001B[37m","\u001B[32m","\u001B[31m","\u001B[36m","\u001B[33m","\u001B[37m")

    private fun format(type: Int, message: String): String {
        return "${colour[type]} ${emojis[type]} $message \u001B[0m"
    }

    fun log(message: String) {
        println(format(0, message))
    }
    fun success(message: String) {
        println(format(1, message))
    }
    fun error(message: String) {
        println(format(2, message))
    }
    fun info(message: String) {
        println(format(3, message))
    }
    fun warn(message: String) {
        println(format(4, message))
    }
    fun debug(message: String) {
        println(format(5, message))
    }
}