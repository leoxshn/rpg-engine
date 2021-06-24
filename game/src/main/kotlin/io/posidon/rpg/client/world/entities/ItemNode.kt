package io.posidon.rpg.client.world.entities

import io.posidon.rpg.client.world.Composition
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.Global
import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.gfx.renderer.renderQuad2D
import io.posidon.uranium.mathlib.clamp
import io.posidon.uranium.scene.node.container.ChunkMap2D
import io.posidon.uranium.scene.node.container.minusAssign
import io.posidon.uranium.window.Window
import kotlin.math.max
import kotlin.random.Random

class ItemNode(
    position: Vec2f,
    composition: Composition
) : EntityNode(position, composition) {

    val velocity = Vec2f.zero()

    private val shader by quadShader("/shaders/objects/debris.fsh")

    private val noiseOffset = Random.nextFloat()

    override fun render(renderer: Renderer, window: Window) {
        shader {
            "millis" set Global.millis().toFloat()
            "noise_offset" set noiseOffset
            "break_progress" set breakProgress
        }
        renderer.renderQuad2D(window, shader, position.x, position.y, .4f, .4f)
    }

    override fun tryBreak(delta: Float, chunkMap: ChunkMap2D) {

    }

    override fun update(delta: Float) {
        position.selfAdd(velocity * delta)
        velocity.selfMultiply(max(1f - delta, 0f))
    }
}
