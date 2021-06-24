package io.posidon.rpg.client.world.entities

import io.posidon.rpg.client.world.Composition
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.scene.node.container.ChunkMap2D
import io.posidon.uranium.tools.InteractiveObject2D

abstract class EntityNode(
    position: Vec2f,
    val composition: Composition
) : InteractiveObject2D(position) {

    var breakProgress = 0f

    abstract fun tryBreak(delta: Float, chunkMap: ChunkMap2D)
}
