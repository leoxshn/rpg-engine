package io.posidon.rpg.world

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.Global
import io.posidon.rpgengine.gfx.assets.invoke
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.Positional
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

class EntityNode(
    override val position: Vec2f
) : Node(), Positional<Vec2f> {

    private val shader by quadShader("/shaders/debris.fsh")

    override fun render(renderer: Renderer, window: Window) {
        shader {
            "millis" set Global.millis().toFloat()
        }
        renderer.renderQuad(window, shader, position.x, position.y, 1f, 1f)
    }
}
