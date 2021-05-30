package io.posidon.rpg.client.world

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.Global
import io.posidon.rpgengine.gfx.assets.invoke
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.gfx.renderer.renderQuad2D
import io.posidon.rpgengine.scene.Positional
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window
import kotlin.random.Random

class EntityNode(
    override val position: Vec2f
) : Node(), Positional<Vec2f> {

    private val shader by quadShader("/shaders/debris.fsh")

    private val noiseOffset = Random.nextFloat()

    override fun render(renderer: Renderer, window: Window) {
        shader {
            "millis" set Global.millis().toFloat()
            "noise_offset" set noiseOffset
        }
        renderer.renderQuad2D(window, shader, position.x, position.y, 1f, 1f)
    }
}
