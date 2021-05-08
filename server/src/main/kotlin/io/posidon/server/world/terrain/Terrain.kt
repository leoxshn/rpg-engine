package io.posidon.server.world.terrain

import io.posidon.game.shared.types.Vec2i
import io.posidon.server.world.Chunk
import io.posidon.server.net.Player
import io.posidon.game.netApi.world.Block
import io.posidon.server.world.World
import io.posidon.server.world.WorldSaver
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class Terrain(
    seed: Long,
    val sizeInChunks: Int,
    val saver: WorldSaver
) {

    inline val sizeInVoxels get() = sizeInChunks * Chunk.SIZE

    val chunks = arrayOfNulls<Chunk>(sizeInChunks * sizeInChunks)

    val generator = WorldGenerator(seed, sizeInVoxels)

    fun getHeight(x: Int, z: Int): Int = generator.getHeight(x, z)

    private val chunkLock = ReentrantLock()

    private val unsafeChunkAccessor = UnsafeChunkAccessor(this)
    private val safeChunkAccessor = SafeChunkAccessor(this)

    fun getBlock(x: Int, y: Int, h: Int): Block? {
        return getChunk(
            x / Chunk.SIZE, y / Chunk.SIZE
        )[x % Chunk.SIZE, y % Chunk.SIZE, h % Chunk.HEIGHT]
    }

    fun setBlock(x: Int, y: Int, h: Int, block: Block?) {
        getChunk(
            x / Chunk.SIZE, y / Chunk.SIZE
        )[x % Chunk.SIZE, y % Chunk.SIZE, h % Chunk.HEIGHT] = block
    }

    fun getChunk(x: Int, y: Int): Chunk {
        return safeChunkAccessor.getChunk(x, y)
    }

    fun withLock(block: (UnsafeChunkAccessor) -> Unit) {
        chunkLock.lock()
        block(unsafeChunkAccessor)
        chunkLock.unlock()
    }

    fun generateAndSaveAllChunks() {
        /*
        val allChunks = sizeInChunks * sizeInChunks * heightInChunks
        var currentChunks = 0
        Console.beforeCmdLine {
            Console.println(Console.colors.BLUE_BRIGHT + "Loading chunks...")
        }
        val threads = LinkedList<Thread>()
        for (x in 0 until sizeInChunks) {
            threads.add(thread {
                for (z in 0 until sizeInChunks) {
                    for (y in 0 until heightInChunks) {
                        if (!saver.hasChunk(x, y, z)) {
                            saver.saveChunk(x, y, z, generator.genChunk(x, y, z))
                        }
                        currentChunks++
                    }
                }
                Console.beforeCmdLine {
                    Console.printInfo((currentChunks * 100 / (allChunks)).toString() + "% chunks loaded", " ($currentChunks out of $allChunks)")
                }
            })
        }
        threads.forEach { it.join() }
        Console.beforeCmdLine {
            Console.println(Console.colors.GREEN_BOLD_BRIGHT + "Done loading chunks!")
        }*/

        run {
            print("test")
            val s = System.currentTimeMillis()
            val threads = LinkedList<Thread>()
            for (x in 0 until sizeInChunks) {
                threads.add(thread {
                    for (z in 0 until sizeInChunks) for (y in 0 until sizeInChunks) {
                        generator.genChunk(x, y)
                        print('.')
                    }
                })
            }
            threads.forEach { it.join() }
            val t = System.currentTimeMillis() - s
            println("test end: $t")
        }
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
            return getExistingChunk(x, y) ?: genChunk(x, y)
        }

        inline fun getExistingChunk(x: Int, y: Int): Chunk? {
            return getLoadedChunk(x, y) ?: getSavedChunk(x, y)
        }

        abstract fun genChunk(x: Int, y: Int): Chunk
        abstract fun getSavedChunk(x: Int, y: Int): Chunk?
    }

    class UnsafeChunkAccessor(terrain: Terrain) : ChunkAccessor(terrain) {
        override fun genChunk(x: Int, y: Int): Chunk {
            return terrain.generator.genChunk(x, y).also {
                setLoadedChunk(x, y, it)
                terrain.saver.saveChunk(x, y, it)
            }
        }
        override fun getSavedChunk(x: Int, y: Int): Chunk? {
            return terrain.saver.loadChunk(x, y)?.also {
                setLoadedChunk(x, y, it)
            }
        }
    }

    class SafeChunkAccessor(terrain: Terrain) : ChunkAccessor(terrain) {
        override fun genChunk(x: Int, y: Int): Chunk {
            return terrain.generator.genChunk(x, y).also {
                terrain.chunkLock.lock()
                setLoadedChunk(x, y, it)
                terrain.saver.saveChunk(x, y, it)
                terrain.chunkLock.unlock()
            }
        }
        override fun getSavedChunk(x: Int, y: Int): Chunk? {
            return terrain.saver.loadChunk(x, y)?.also {
                terrain.chunkLock.lock()
                setLoadedChunk(x, y, it)
                terrain.chunkLock.unlock()
            }
        }
    }
}