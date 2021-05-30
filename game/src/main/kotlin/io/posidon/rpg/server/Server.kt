package io.posidon.rpg.server

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpg.client.world.EntityNode
import io.posidon.rpg.client.world.Player
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.scene.node.container.ChunkMap2D
import io.posidon.rpgengine.scene.node.container.plusAssign
import kotlin.random.Random

class Server(
    val chunkMap: ChunkMap2D,
    val player: Player
) : Node() {

    fun createChunk() {

    }

    private var secsSincePopulation = 0f
    private val secsPerPopulation = 12f
    override fun update(delta: Float) {
        secsSincePopulation += delta
        if (secsSincePopulation >= secsPerPopulation) {
            secsSincePopulation -= secsPerPopulation
            populateAroundPosition(player.position)
        }
    }

    fun populateAroundPosition(pos: Vec2f) {
        val random = Random(pos.longUID())
        for (i in 0..3) {
            val x = random.nextDouble(-10.0, 10.0).toFloat()
            val y = random.nextDouble(-10.0, 10.0).toFloat()
            chunkMap += EntityNode(Vec2f(x, y).apply { selfAdd(pos) })
        }
    }
}
