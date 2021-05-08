package io.posidon.server.world.terrain

import io.posidon.game.shared.fastSqrt
import io.posidon.server.world.Chunk
import io.posidon.server.tools.OpenSimplex2STileableXZ
import io.posidon.game.netApi.world.Block
import kotlin.math.*
import kotlin.random.Random

class WorldGenerator(seed: Long, val mapSize: Int) {

    val maxHeight = Chunk.HEIGHT

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

    private val heightsAndFlatnesses = DoubleArray(mapSize * mapSize * 2) { -1.0 }

    /**
     * Height is in the range [0.0, mapHeight]
     * Flatness is in the range [0.0, 1.0]
     */
    private inline fun getHeightAndFlatness(absX: Double, absZ: Double): Pair<Double, Double> {
        //val startTime = System.currentTimeMillis()
        val i = absX.toInt() * mapSize + absZ.toInt()
        return (if (heightsAndFlatnesses[i * 2] != -1.0) heightsAndFlatnesses[i * 2] to heightsAndFlatnesses[i * 2 + 1] else {
            val terracing = (openSimplex192.get(seedA, absX, absZ, offset = 25.0).pow(0.2) + 1) / 2

            val flatness = run {
                val a = openSimplex192.get(seedA, absX, absZ, offset = 300.0).toFloat()
                when {
                    a > 0.0 -> (fastSqrt(a) + 1) / 2.0
                    a < 0.0 -> (-fastSqrt(-a) + 1) / 2.0
                    else -> 0.0
                }.pow(openSimplex192.get(seedB, absX, absZ) + 1)
            }

            val mountainHeight =
                ((openSimplex128.get(seedC, absX, absZ) + 1) / 2).pow(3) *
                ((openSimplex192.get(seedD, absX, absZ) + 1) / 2).pow(1.2) *
                (1.6 - abs(openSimplex192.get(seedC, absX, absZ))) *
                min(1.0, max(0.2, abs(openSimplex256.get(seedC, absX, absZ)).times(2.0).pow(0.5).times(2.0)))

            var height = 136 + mountainHeight * 256
            val invFlatness = 1 - flatness
            height += openSimplex48.get(seedD, absX, absZ) * (28 * invFlatness)
            height += openSimplex8.get(seedA, absX, absZ) * (7 * invFlatness)

            if (terracing > 0.0) {
                val smoothHeight = height
                val terracingAmount = 12
                height /= terracingAmount
                val ih = round(height)
                height = (ih + 0.5 * (2 * (height - ih)).pow(11)) * terracingAmount
                height = smoothHeight * (1 - terracing) + height * terracing
            }

            heightsAndFlatnesses[i * 2] = height
            heightsAndFlatnesses[i * 2 + 1] = flatness
            height to flatness
        }).also {
            //val totalTime = System.currentTimeMillis() - startTime
            //heightTime += totalTime
            //heightCount++
        }
    }

    private fun genVoxel(absX: Double, absY: Double, absZ: Double, height: Double, flatness: Double): Boolean {
        //val startTime = System.nanoTime()

        if (absY < height - 5.0) return true

        // Height
        var blockF = max(min(5.0, (height - absY) / 8.0), -5.0)

        val invFlatness = 1 - flatness
        val microMultiplier = 0.1 + 0.2 * invFlatness
        val bigFMultiplier = 1.0 + 3.2 * invFlatness
        val overhangMultiplier = 1.0 + 2.8 * invFlatness
        val threshold = 0.5

        blockF += openSimplex48.get(seedA, absX, absY * 1.8, absZ, offset = 12.0) * bigFMultiplier

        blockF += openSimplex8.get(seedB, absX, absY, absZ, offset = 24.0) * microMultiplier

        if (blockF > threshold) {
            return true
        }

        // Overhangs
        blockF += (1 - abs(openSimplex48.get(seedB, absX, absY, absZ))).pow(2) * overhangMultiplier

        //val totalTime = System.nanoTime() - startTime
        //voxelTime += totalTime
        //voxelCount++

        return blockF > threshold
    }

    fun genChunk(chunkX: Int, chunkY: Int): Chunk {
        //val start = System.nanoTime()
        val chunk = Chunk()
        val absChunkX = chunkX * Chunk.SIZE
        val absChunkY = chunkY * Chunk.SIZE

        for (x in 0 until Chunk.SIZE) for (z in 0 until Chunk.SIZE) {

            val absX = (absChunkX + x).toDouble()
            val absZ = (absChunkY + z).toDouble()

            val (height, flatness) = getHeightAndFlatness(absX, absZ)

            for (y in 0 until Chunk.HEIGHT) {
                val absY = (absChunkY + y).toDouble()
                if (genVoxel(absX, absY, absZ, height, flatness)) {
                    chunk[x, y, z] = Block.STONE
                }
            }
/*
            for (y in 0 until Chunk.HEIGHT) {
                val absY = (absChunkY + y).toDouble()
                if (chunk[x, y, z] != null) {
                    if (getCave(absX, absY, absZ, height, flatness)) {
                        chunk[x, y, z] = null
                    } else if (if (y == Chunk.HEIGHT - 1) !genVoxel(absX, absY + 1, absZ, height, flatness) else chunk[x, y + 1, z] == null) {
                        chunk[x, y, z] = Block.DIRT
                    }
                }
            }*/
        }
        return chunk
    }

    fun getHeight(x: Int, z: Int): Int = getHeightAndFlatness(x.toDouble(), z.toDouble()).first.toInt()
}