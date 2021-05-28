package io.posidon.server.net

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.PacketTypes
import io.posidon.server.cli.Console
import io.posidon.server.world.World
import io.posidon.server.print

object ReceivedPacketHandler {
    operator fun invoke(player: Player, packet: Packet) { try {
        val tokens = packet.tokens
        when (tokens[0]) {
            PacketTypes.GOTO -> {
                val p = packet.parsePosition()
                player.triggerTickEvent {
                    position.set(p)
                }
            }
            else -> Console.beforeCmdLine {
                Console.printProblem(player.name, " sent an unknown packet: $packet")
            }
        }
    } catch (e: Exception) {
        Console.beforeCmdLine {
            Console.printProblem(player.name, " sent an packet that couldn't be processed: $packet")
            e.print()
        }
    }}
}