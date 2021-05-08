package io.posidon.server.world

import io.posidon.game.netApi.server.ServerApi
import io.posidon.game.shared.types.Vec2f
import io.posidon.game.shared.types.Vec2i
import io.posidon.server.Server
import io.posidon.server.cli.Console
import io.posidon.server.loop
import io.posidon.server.net.Player
import io.posidon.server.world.terrain.Terrain
import kotlin.concurrent.thread

object World {

    val sizeInChunks: Int = 16

    private lateinit var terrain: Terrain
    private val saver = WorldSaver("world")

    fun init(seed: Long) {
        terrain = Terrain(seed, sizeInChunks, saver)
        //terrain.generateAndSaveAllChunks()
        thread {
            var lastTime: Long = System.nanoTime()
            var delta = 0.0
            loop {
                val now: Long = System.nanoTime()
                delta += (now - lastTime) / 1000000000.0
                lastTime = now
                while (delta >= secPerTick) {
                    terrain.withLock { unsafe ->
                        try {
                            for (x in 0 until sizeInChunks)
                                for (y in 0 until sizeInChunks)
                                    if (unsafe.getLoadedChunk(x, y) != null) {
                                        val r = Vec2f(x.toFloat() * Chunk.SIZE, y.toFloat() * Chunk.SIZE)
                                        var shouldDelete = true
                                        for (player in Server.players) {
                                            if (r.apply { selfSubtract(player.position.xy) }.length < deletionDistance) {
                                                shouldDelete = false
                                                break
                                            } else {
                                                player.sentChunks.remove(Vec2i(x, y))
                                            }
                                        }
                                        if (shouldDelete) {
                                            unsafe.setLoadedChunk(x, y, null)
                                        }
                                    }
                        } catch (e: OutOfMemoryError) {
                            System.gc()
                            Console.beforeCmdLine {
                                Console.printProblem("OutOfMemoryError", " in world")
                                e.printStackTrace()
                            }
                        }
                    }
                    delta -= secPerTick
                }
            }
        }
    }

    private val deletionDistance = 400f
    private val secPerTick = 2.0

    fun sendChunks(player: Player) {
        val xx = (player.position.x / Chunk.SIZE).toInt()
        val yy = (player.position.y / Chunk.SIZE).toInt()
        val r = 2

        terrain.sendChunksIncrementally(player, xx, yy, r)
    }

    fun getDefaultSpawnPosition(): Int {
        terrain.getChunk(0, 0)
        return terrain.getHeight(0, 0) + 5
    }

    fun breakBlock(player: Player, x: Int, y: Int, h: Int) {
        terrain.setBlock(x, y, h, null)
        player.send(ServerApi.block(x, y, h, -1))
    }
}