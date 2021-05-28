package io.posidon.server.net

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.server.ServerApi
import io.posidon.game.netApi.server.player.ServerPlayer
import io.posidon.game.netApi.util.Compressor
import io.posidon.game.shared.Format
import io.posidon.game.shared.types.Vec2f
import io.posidon.game.shared.types.Vec2i
import io.posidon.server.cli.Console
import io.posidon.server.world.World
import io.posidon.server.world.terrain.Chunk
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

class Player(
    socket: Socket
) : ServerPlayer<Player>(socket) {

    private val tickEventQueue = ConcurrentLinkedQueue<Player.() -> Unit>()

    val sentChunks = ArrayList<Vec2i>()

    fun triggerTickEvent(it: Player.() -> Unit) = tickEventQueue.add(it)

    val position = Vec2f.zero()

    var moveSpeed = 0.5f
    var jumpHeight = 0.5f

    fun sendChunk(chunkPos: Vec2i, chunk: Chunk) {
        send(ServerApi.chunk(chunkPos.x, chunkPos.y, chunk.makePacketString().let { Format.newLineUnescape(Compressor.compressString(it, 1024 * 6)) }))
        sentChunks.add(chunkPos)
    }

    fun start() {
        startListenForPackets()

        thread {
            var lastTime: Long = System.nanoTime()
            var delta = 0.0

            while (running) {
                val now: Long = System.nanoTime()
                delta += (now - lastTime) / 1000000000.0
                lastTime = now
                if (delta >= 0.01) {
                    tick()
                    delta--
                }
            }
        }
    }

    private inline fun tick() {
        if (tickEventQueue.size > 3) Console.beforeCmdLine {
            Console.printProblem(name, " is sending packets to fast! (${tickEventQueue.size} per tick)")
        }
        tickEventQueue.removeIf { it(); true }

        World.sendChunks(this)
    }

    override fun onPacketReceived(packet: Packet) {
        ReceivedPacketHandler(this, packet)
    }

    override fun onDisconnect() {
        Console.beforeCmdLine { Console.printInfo(name, " left the server") }
    }
}