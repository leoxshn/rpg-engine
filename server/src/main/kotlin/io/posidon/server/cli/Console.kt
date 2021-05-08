package io.posidon.server.cli

import io.posidon.game.netApi.server.ServerApi
import io.posidon.game.shared.ConsoleColors
import io.posidon.server.Globals
import io.posidon.server.Server
import io.posidon.server.loop
import io.posidon.server.net.Chat
import io.posidon.server.stop
import kotlin.concurrent.thread

class Console : Runnable {

    companion object {
        val errHighlightColor = ConsoleColors.YELLOW_BOLD_BRIGHT
        val errColor = ConsoleColors.RED
        const val prefix = "> "

        inline fun println(string: String) = kotlin.io.println(string + ConsoleColors.RESET)
        inline fun print(string: String) = kotlin.io.print(string + ConsoleColors.RESET)
        inline fun printProblem(highlight: String, otherText: String) = println(errHighlightColor + highlight + errColor + otherText)
        inline fun printInfo(highlight: String, otherText: String) = println(ConsoleColors.CYAN_BOLD_BRIGHT + highlight + ConsoleColors.BLUE + otherText)

        inline fun backspace(characters: Int) {
            for (i in 0 until characters) print('\b')
        }

        inline fun beforeCmdLine(stuff: () -> Unit) {
            backspace(prefix.length)
            stuff()
            print(ConsoleColors.BLUE_BOLD + prefix)
        }
    }

    override fun run() {
        loop {
            print(ConsoleColors.BLUE_BOLD + prefix)
            val line = readLine()!!
            val cmd = line.split(' ')
            when (cmd[0]) {
                "", " " -> {}
                "stop" -> stop()
                "ip" -> {
                    thread (isDaemon = true) {
                        val ip = Server.getExtIP()
                        beforeCmdLine {
                            println(ip?.let { ConsoleColors.PURPLE_BOLD + "ip" + ConsoleColors.PURPLE + " -> " + ConsoleColors.RESET + it } ?: ConsoleColors.RED + "error: couldn't get external ip")
                        }
                    }
                }
                "kick" -> {
                    if (cmd.size == 1) println(errColor + "kick who?")
                    else for (i in 1 until cmd.size) {
                        Server.players[cmd[i]]?.disconnect()
                            ?: println(errColor + "there's no player called " + errHighlightColor + cmd[i] + errColor + " on the server")
                    }
                }
                "players" -> {
                    if (Server.players.isEmpty) println("There are no players online")
                    else for (player in Server.players) println(player.name)
                }
                "time" -> {
                    if (cmd.size == 1) println(ConsoleColors.PURPLE_BOLD + "time" + ConsoleColors.PURPLE + " = " + ConsoleColors.RESET + Globals.time)
                    else {
                        when (cmd[1]) {
                            "=", "set" -> {
                                if (cmd.size == 3) {
                                    cmd[2].toDoubleOrNull()?.let {
                                        Globals.time = it
                                        Server.sendToAllPlayers(ServerApi.time(Globals.time))
                                        println("Time set to ${Globals.time}")
                                    } ?: println(errColor + cmd[2] + " isn't a number!")
                                } else println(errColor + "set time to what?")
                            }
                            "+=", "add" -> {
                                if (cmd.size == 3) {
                                    cmd[2].toDoubleOrNull()?.let {
                                        Globals.time += it
                                        Server.sendToAllPlayers(ServerApi.time(Globals.time))
                                        println("Time set to ${Globals.time}")
                                    } ?: println(errColor + cmd[2] + " isn't a number!")
                                } else println(errColor + "add what to time?")
                            }
                            else -> { printProblem(cmd[1], " isn't a valid parameter!") }
                        }
                    }
                }
                "ch" -> {
                    Chat.post("[server]", line.substring(3))
                    printInfo("[server]:", ' ' + line.substring(3))
                }
                else -> printProblem(cmd[0], " isn't a valid command!")
            }
        }
    }
}