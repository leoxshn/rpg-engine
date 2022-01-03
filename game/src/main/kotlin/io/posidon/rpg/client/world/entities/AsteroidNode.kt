package io.posidon.rpg.client.world.entities

import io.posidon.rpg.client.world.Composition
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.Global
import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.gfx.renderer.renderQuad2D
import io.posidon.uranium.mathlib.types.functions.toRawBits
import io.posidon.uranium.scene.node.container.ChunkMap2D
import io.posidon.uranium.scene.node.container.minusAssign
import io.posidon.uranium.scene.node.container.plusAssign
import io.posidon.uranium.window.Window
import kotlin.random.Random

class AsteroidNode(
    position: Vec2f,
    composition: Composition
) : EntityNode(position, composition) {

    private val shader by objectShader("/shaders/objects/debris.fsh")

    private val noiseOffset = Random.nextFloat()

    override fun render(renderer: Renderer, window: Window) {
        shader {
            "millis" set Global.millis().toFloat()
            "noise_offset" set noiseOffset
            "break_progress" set breakProgress
        }
        renderer.renderQuad2D(window, shader, position.x, position.y, 1f, 1f)
    }

    private val breakSpeed = .3f

    override fun tryBreak(delta: Float, chunkMap: ChunkMap2D) {
        breakProgress += delta * breakSpeed * 2
        if (breakProgress >= 1f) {
            explode(chunkMap)
            breakProgress = 0f
        }
    }

    override fun update(delta: Float) {
        if (breakProgress > 0f) {
            breakProgress -= delta * breakSpeed
            if (breakProgress < 0f) breakProgress = 0f
        }
    }

    private fun explode(chunkMap: ChunkMap2D) {
        chunkMap -= this
        val random = Random(position.toRawBits())
        repeat(6) {
            chunkMap += ItemNode(position.copy(), composition).apply {
                val x = (random.nextFloat() - .5f) * 5f
                val y = (random.nextFloat() - .5f) * 5f
                velocity.set(x, y)
            }
        }
    }
}
