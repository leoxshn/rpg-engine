package io.posidon.rpg.client.world

import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.gfx.renderer.renderQuad2D
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.window.Window
import kotlin.math.PI

class Indicators(val player: Player) : Node() {

    val entityQuad by quadShader("/shaders/entity_quad.fsh")

    override fun render(renderer: Renderer, window: Window) {
        player.closestEntities.forEachIndexed { i, entity ->
            val isSelected = i == player.selectionI
            entityQuad {
                "is_chosen" set isSelected
            }
            val s = if (isSelected) 1.2f - entity.breakProgress * .4f else 1f
            val r = if (isSelected) 1f + entity.breakProgress * 8f else 1f
            renderer.renderQuad2D(window, entityQuad, entity.position, s, s, rotationZ = (PI / 4f).toFloat() * r)
        }
    }
}
