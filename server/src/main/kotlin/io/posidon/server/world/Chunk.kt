package io.posidon.server.world

import io.posidon.game.netApi.util.Compressor
import io.posidon.game.netApi.world.Block
import io.posidon.game.shared.types.Vec3i
import java.io.*
import java.util.*

class Chunk(
    private val blocks: Array<Block?> = arrayOfNulls(VOLUME)
) {

    operator fun get(i: Int): Block? = blocks[i]
    inline operator fun get(pos: Vec3i) = get(pos.x, pos.y, pos.z)
    inline operator fun get(x: Int, y: Int, h: Int): Block? = get(x * SIZE * SIZE + y * SIZE + h)
    operator fun set(i: Int, block: Block?) { blocks[i] = block }
    inline operator fun set(pos: Vec3i, block: Block?) = set(pos.x, pos.y, pos.z, block)
    inline operator fun set(x: Int, y: Int, h: Int, block: Block?) =
        set(x * SIZE * SIZE + y * SIZE + h, block)

    val indices get() = blocks.indices

    fun makePacketString(): String? {
        val stringBuilder = StringBuilder()
        var nullCount = 0
        for (i in blocks.indices) {
            val block = get(i)
            if (block == null) nullCount++
            else {
                if (nullCount != 0) {
                    for (j in 0 until nullCount) {
                        stringBuilder
                            .append((-1 ushr 16).toChar())
                            .append((-1).toChar())
                    }
                    nullCount = 0
                }
                stringBuilder
                    .append((block.ordinal ushr 16).toChar())
                    .append((block.ordinal).toChar())
            }
        }
        return if (nullCount == VOLUME) null else stringBuilder.toString()
    }

    fun getSaveBytes(): ByteArray {
        val dictionary = HashMap<Int, String>()
        val (blocks, bl) = ByteArrayOutputStream().use {
            var nullCount = 0
            for (i in blocks.indices) {
                val block = get(i)
                if (block == null) nullCount++
                else {
                    if (nullCount != 0) {
                        for (j in 0 until nullCount) {
                            it.write(-1)
                        }
                        nullCount = 0
                    }
                    dictionary.putIfAbsent(block.ordinal, block.getSaveString())
                    it.write(block.ordinal)
                }
            }
            if (nullCount == VOLUME) {
                ByteArray(0) to 0
            } else {
                val b = it.toByteArray()
                b to b.size
            }
        }
        return if (bl != 0) {
            ByteArrayOutputStream().use {
                val db = ByteArrayOutputStream().use {
                    ObjectOutputStream(it).use { it.writeObject(dictionary) }
                    it.toByteArray()
                }
                it.write(db)
                it.write(blocks, 0, bl)
                val b = it.toByteArray()
                Compressor.compress(b, b.size).let { it.first.copyOf(it.second) }
            }
        } else blocks
    }

    companion object {
        const val SIZE = 16
        const val HEIGHT = 16
        const val AREA = SIZE * SIZE
        const val VOLUME = AREA * HEIGHT

        fun readFromInputStream(inputStream: InputStream): Chunk? {
            if (inputStream.available() == 0) return null
            val (b, bl) = Compressor.decompress(inputStream.readAllBytes(), VOLUME * 4 + 128)
            val input = ByteArrayInputStream(b.copyOf(bl))
            val db = ObjectInputStream(input).use {
                it.readObject() as HashMap<Int, String>
            }
            val blockDictionary = HashMap<Int, Block>().apply {
                for ((i, s) in db) {
                    Block.values().find { it.id == s }?.let { put(i, it) }
                }
            }
            val blocks = Array(VOLUME) {
                val k = input.read()
                if (k == -1 || input.available() == 0) null
                else blockDictionary[k]
            }

            return Chunk(blocks)
        }
    }
}