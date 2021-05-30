package io.posidon.rpg.client.world

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.Global
import io.posidon.rpgengine.gfx.assets.invoke
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.gfx.renderer.renderMesh2D
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

class Player : Node() {

    val position = Vec2f.zero()
    val moveSpeed = 7.5f
    val acceleration = 12f

    val body by mesh(0, 1, 2) {
        2.v(-.5f, -.5f, .5f, -.5f, 0f, .6f)
    }

    val shader by quadShader("/shaders/player.fsh")

    val spriteWidth = 1f
    val spriteHeight = 1.6f

    private var rotation = 0f
    private var targetRotation = 0f
    private var choppedRotation = 0f

    override fun render(renderer: Renderer, window: Window) {
        shader {
            "millis" set Global.millis().toFloat()
        }
        renderer.renderMesh2D(body, window, shader, position.x, position.y, spriteWidth, spriteHeight, choppedRotation)
    }

    val velocity = Vec2f.zero()
    private var scr = 0f
    private val animationFrameSeconds = 1f / 24f
    override fun update(delta: Float) {
        val dir = input.getWalkDirection()
        velocity.selfMix(dir * moveSpeed * delta, acceleration * delta)
        position.selfAdd(velocity)
        if (!dir.isZero) {
            val a = atan2(dir.x, dir.y)
            val b = a + 2.0 * PI
            val c = a - 2.0 * PI
            val ab = if (abs(a - rotation) < abs(b - rotation)) a else b.toFloat()
            targetRotation = if (abs(ab - rotation) < abs(c - rotation)) ab else c.toFloat()
            val smoothing = (1f - delta * 10f)
            rotation = rotation * smoothing + targetRotation * (1f - smoothing)
            rotation %= (2.0 * PI).toFloat()
        }
        if (!velocity.isZero) {
            scr += delta
            if (scr >= animationFrameSeconds) {
                scr -= animationFrameSeconds
                choppedRotation = rotation
            }
        } else {
            scr = 0f
        }
    }

    override fun destroy() {
        shader.destroy()
    }
}
