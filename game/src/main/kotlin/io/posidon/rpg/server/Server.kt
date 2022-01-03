package io.posidon.rpg.server

import io.posidon.rpg.client.world.entities.AsteroidNode
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.mathlib.types.Vec2i
import io.posidon.uranium.mathlib.types.functions.*
import io.posidon.rpg.client.world.Composition
import io.posidon.rpg.client.world.Player
import io.posidon.uranium.mathlib.types.functions.toRawBits
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.scene.node.container.ChunkMap
import io.posidon.uranium.scene.node.container.ChunkMap2D
import io.posidon.uranium.scene.node.container.plusAssign
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Server(
    val chunkMap: ChunkMap2D,
    val player: Player
) : Node() {

    fun createChunk(pos: Vec2i): ChunkMap.Chunk<Vec2i> {
        val chunk = ChunkMap.Chunk(chunkMap)
        val random = Random(pos.toRawBits())
        for (i in 4..random.nextInt(32)) {
            val x = random.nextDouble(0.0, chunkMap.chunkSize.toDouble()).toFloat()
            val y = random.nextDouble(0.0, chunkMap.chunkSize.toDouble()).toFloat()
            chunk += AsteroidNode(pos.toVec2f().apply {
                selfMultiply(chunkMap.chunkSize)
                selfAdd(Vec2f(x, y))
            }, generateComposition())
        }
        return chunk
    }

    private fun generateComposition(): Composition {
        val r = Random.nextInt(500)
        val g = Random.nextInt(500)
        val t = (g + r + 500)
        val s = min(max(1000 - t * t / 1000, 0), 1000 - (r + g))
        val b = 1000 - (r + g + s)
        return Composition(r / 10f, g / 10f, b / 10f, s / 10f)
    }

    private val secsPerPopulation = 1f
    private var secsSincePopulation = secsPerPopulation
    override fun update(delta: Float) {
        secsSincePopulation += delta
        if (secsSincePopulation >= secsPerPopulation) {
            secsSincePopulation -= secsPerPopulation
            populateAroundPosition(player.position)
        }
    }

    fun populateAroundPosition(pos: Vec2f) {
        val chunkPos = pos.toVec2i() / chunkMap.chunkSize
        for (x in -2..1) for (y in -2..1) {
            val c = chunkPos + Vec2i(x, y)
            chunkMap.chunks.computeIfAbsent(c, ::createChunk)
        }
    }
}
