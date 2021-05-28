package io.posidon.server.world.terrain

import io.posidon.game.shared.types.Vec2i
import io.posidon.server.net.Player
import io.posidon.server.world.World
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class Terrain(
    seed: Long,
    val sizeInChunks: Int
) {

    inline val sizeInVoxels get() = sizeInChunks * Chunk.SIZE

    val chunks = arrayOfNulls<Chunk>(sizeInChunks * sizeInChunks)

    val generator = WorldGenerator(seed, sizeInVoxels)

    private val chunkLock = ReentrantLock()

    private val unsafeChunkAccessor = UnsafeChunkAccessor(this)
    private val safeChunkAccessor = SafeChunkAccessor(this)

    fun probe(x: Int, y: Int): Probe {
        val chunk = getChunk(x / Chunk.SIZE, y / Chunk.SIZE)
        return chunk.probe(x % Chunk.SIZE, y % Chunk.SIZE)
    }

    fun set(x: Int, y: Int, probe: Probe) {
        getChunk(x / Chunk.SIZE, y / Chunk.SIZE).set(x % Chunk.SIZE, y % Chunk.SIZE, probe)
    }

    fun getChunk(x: Int, y: Int): Chunk {
        return safeChunkAccessor.getChunk(x, y)
    }

    fun withLock(block: (UnsafeChunkAccessor) -> Unit) {
        chunkLock.lock()
        block(unsafeChunkAccessor)
        chunkLock.unlock()
    }

    fun sendChunk(player: Player, accessor: ChunkAccessor, x: Int, y: Int) {
        val chunkPos = Vec2i(x, y)
        if (!player.sentChunks.contains(chunkPos)) {
            val chunk = accessor.getChunk(chunkPos)
            player.sendChunk(chunkPos, chunk)
        }
    }

    fun sendChunksIncrementally(player: Player, xx: Int, yy: Int, radius: Int) {
        thread(isDaemon = true) {
            sendChunk(player, safeChunkAccessor, xx, yy)
        }
        for (r in 1..radius) thread(isDaemon = true) {
            run {
                val z0 = run {
                    val c = (yy - r) % World.sizeInChunks
                    if (c < 0) World.sizeInChunks + c else c
                }
                val z1 = run {
                    val c = (yy + r) % World.sizeInChunks
                    if (c < 0) World.sizeInChunks + c else c
                }
                for (_x in xx - r..xx + r) {
                    val x = run {
                        val c = _x % World.sizeInChunks
                        if (c < 0) World.sizeInChunks + c else c
                    }
                    sendChunk(player, safeChunkAccessor, x, z0)
                    sendChunk(player, safeChunkAccessor, x, z1)
                }
            }
            val x0 = run {
                val c = (xx - r) % World.sizeInChunks
                if (c < 0) World.sizeInChunks + c else c
            }
            val x1 = run {
                val c = (xx + r) % World.sizeInChunks
                if (c < 0) World.sizeInChunks + c else c
            }
            for (_z in yy - r + 1..yy - 1 + r) {
                val z = run {
                    val c = _z % World.sizeInChunks
                    if (c < 0) World.sizeInChunks + c else c
                }
                sendChunk(player, safeChunkAccessor, x0, z)
                sendChunk(player, safeChunkAccessor, x1, z)
            }
        }
    }

    abstract class ChunkAccessor(val terrain: Terrain) {

        fun getLoadedChunk(x: Int, y: Int): Chunk? {
            when {
                x < 0 || x >= World.sizeInChunks -> throw IllegalArgumentException("x = $x")
                y < 0 || y >= World.sizeInChunks -> throw IllegalArgumentException("y = $y")
            }
            return terrain.chunks[x * World.sizeInChunks + y]
        }
        inline fun setLoadedChunk(x: Int, y: Int, chunk: Chunk?) {
            terrain.chunks[x * World.sizeInChunks + y] = chunk
        }

        inline fun getChunk(chunkPos: Vec2i): Chunk = getChunk(chunkPos.x, chunkPos.y)
        inline fun getChunk(x: Int, y: Int): Chunk {
            return getLoadedChunk(x, y) ?: genChunk(x, y)
        }

        abstract fun genChunk(x: Int, y: Int): Chunk
    }

    class UnsafeChunkAccessor(terrain: Terrain) : ChunkAccessor(terrain) {
        override fun genChunk(x: Int, y: Int): Chunk {
            return terrain.generator.genChunk(x, y).also {
                setLoadedChunk(x, y, it)
            }
        }
    }

    class SafeChunkAccessor(terrain: Terrain) : ChunkAccessor(terrain) {
        override fun genChunk(x: Int, y: Int): Chunk {
            return terrain.generator.genChunk(x, y).also {
                terrain.chunkLock.lock()
                setLoadedChunk(x, y, it)
                terrain.chunkLock.unlock()
            }
        }
    }
}