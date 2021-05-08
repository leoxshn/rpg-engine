package io.posidon.rpgengine.tools

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.renderer.ModifiedRenderer
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

class Camera2D(
    renderer: Renderer,
    val followedPosition: Vec2f
) : Node() {

    val renderer = object : ModifiedRenderer(renderer) {
        override fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, width: Float, height: Float) {
            renderer.renderQuad(window, quadShader, x - xy.x, y - xy.y, width, height)
        }
        override fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, scaleX: Float, scaleY: Float) {
            renderer.renderMesh(mesh, window, shader, x - xy.x, y - xy.y, scaleX, scaleY)
        }
    }

    val xy = Vec2f.zero()
    var speed = 2f
    var acceleration = 4f

    val velocity = Vec2f.zero()
    override fun update(delta: Float) {
        val distance = followedPosition - xy
        val newVelocity = distance * delta * speed
        velocity.selfMix(newVelocity, (delta * acceleration).coerceAtMost(1f))
        xy.selfAdd(velocity)
    }
}
