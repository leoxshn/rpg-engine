package io.posidon.rpg

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.PacketTypes
import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.Global
import io.posidon.rpgengine.gfx.assets.invoke
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

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

    override fun render(renderer: Renderer, window: Window) {
        shader {
            "millis" set Global.millis().toFloat()
        }
        renderer.renderMesh(body, window, shader, position.x, position.y, spriteWidth, spriteHeight)
    }

    val velocity = Vec2f.zero()
    override fun update(delta: Float) {
        val dir = input.getWalkDirection()
        velocity.selfMix(dir * moveSpeed * delta, acceleration * delta)
        position.selfAdd(velocity)
    }

    override fun destroy() {
        shader.destroy()
    }

    fun onPacketReceived(packet: Packet) {
        when (packet.type) {
            PacketTypes.POSITION -> position.set(packet.parsePosition())
        }
    }
}
