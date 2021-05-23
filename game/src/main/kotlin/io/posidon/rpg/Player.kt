package io.posidon.rpg

import io.posidon.game.netApi.Packet
import io.posidon.game.netApi.PacketTypes
import io.posidon.game.shared.types.Position
import io.posidon.rpgengine.gfx.*
import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

class Player : Node() {

    val position = Position.zero()
    val moveSpeed = 7.5f
    val acceleration = 12f

    val shader by onInit { context.loadQuadShader(log, "/shaders/player.fsh") }
    val spriteSheet by onInit {
        context.loadTexture(log, "/textures/player-walk.png").apply {
            setMagFilter(Texture.MagFilter.NEAREST)
            setMinFilter(Texture.MinFilter.NEAREST)
        }
    }

    val spriteSheetWidth = 10

    var currentSprite = 0f

    val runFPS = 15
    val spriteHeight = 1.6f

    override fun render(renderer: Renderer, window: Window) {
        val shader = shader
        val texture = spriteSheet
        shader.bind()
        shader["frame"] = Vec2f(currentSprite, 0f)
        shader["frame_to_sheet_ratio"] = Vec2f(1f / spriteSheetWidth, 1f)
        texture.bind(0)
        renderer.renderQuad(window, shader, position.x, position.y, spriteHeight / texture.height * texture.width / spriteSheetWidth, spriteHeight)
    }

    val velocity = Vec2f.zero()
    var animationDelta = 0f
    override fun update(delta: Float) {
        animationDelta += delta * runFPS
        if (animationDelta >= 1f) {
            animationDelta -= 1
            currentSprite++
            currentSprite %= spriteSheetWidth
        }

        val dir = input.getWalkDirection()
        velocity.selfMix(dir * moveSpeed * delta, acceleration * delta)
        position.xy.selfAdd(velocity)
    }

    override fun destroy() {
        shader.destroy()
        spriteSheet.destroy()
    }

    fun onPacketReceived(packet: Packet) {
        when (packet.type) {
            PacketTypes.POSITION -> position.set(packet.parsePosition())
        }
    }
}