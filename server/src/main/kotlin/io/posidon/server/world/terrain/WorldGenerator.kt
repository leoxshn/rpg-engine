package io.posidon.server.world.terrain

import io.posidon.server.tools.OpenSimplex2STileableXZ
import kotlin.random.Random

class WorldGenerator(seed: Long, val mapSize: Int) {

    private val random = Random(seed)

    private val seedA = random.nextLong()
    private val seedB = random.nextLong()
    private val seedC = random.nextLong()
    private val seedD = random.nextLong()

    private val openSimplex8 = OpenSimplex2STileableXZ(1.0 / 8.0, mapSize)
    private val openSimplex16 = OpenSimplex2STileableXZ(1.0 / 16.0, mapSize)
    private val openSimplex24 = OpenSimplex2STileableXZ(1.0 / 24.0, mapSize)
    private val openSimplex48 = OpenSimplex2STileableXZ(1.0 / 48.0, mapSize)
    private val openSimplex64 = OpenSimplex2STileableXZ(1.0 / 64.0, mapSize)
    private val openSimplex72 = OpenSimplex2STileableXZ(1.0 / 72.0, mapSize)
    private val openSimplex96 = OpenSimplex2STileableXZ(1.0 / 96.0, mapSize)
    private val openSimplex128 = OpenSimplex2STileableXZ(1.0 / 128.0, mapSize)
    private val openSimplex192 = OpenSimplex2STileableXZ(1.0 / 192.0, mapSize)
    private val openSimplex256 = OpenSimplex2STileableXZ(1.0 / 256.0, mapSize)

    fun genChunk(chunkX: Int, chunkY: Int): Chunk {
        return Chunk(
            initTemperature = {
                val x = it / Chunk.SIZE
                val y = it % Chunk.SIZE
                openSimplex16.get(seedA, x.toDouble(), y.toDouble()).toFloat()
            },
            initViscosity = {
                val x = it / Chunk.SIZE
                val y = it % Chunk.SIZE
                openSimplex16.get(seedB, x.toDouble(), y.toDouble()).toFloat()
            }
        )
    }
}