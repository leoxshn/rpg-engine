package io.posidon.uranium.debug

import io.posidon.uranium.util.ConsoleColors

open class Logger : ILogger {
    final override fun d(message: String) = println(message)
    final override fun w(message: String) = println(ConsoleColors.YELLOW_BRIGHT + message + ConsoleColors.RESET)
    final override fun e(message: String) = println(ConsoleColors.RED_BRIGHT + message + ConsoleColors.RESET)
    final override fun i(message: String) = println(ConsoleColors.CYAN_BRIGHT + message + ConsoleColors.RESET)
}