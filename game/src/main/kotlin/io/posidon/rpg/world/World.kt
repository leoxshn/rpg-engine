package io.posidon.rpg.world

import io.posidon.game.netApi.Packet
import io.posidon.game.shared.types.Vec2f
import io.posidon.rpg.Player
import io.posidon.rpgengine.Global
import io.posidon.rpgengine.scene.Scene
import io.posidon.rpgengine.scene.SceneChildrenBuilder
import io.posidon.rpgengine.scene.node.container.ChunkMap2D
import io.posidon.rpgengine.scene.node.container.plusAssign
import io.posidon.rpgengine.scene.node.util.FpsCounter

class World : Scene() {

    val player = Player()
    val chunkMap = ChunkMap2D(32) {
        val onScreen = it.toVec2f().apply {
            selfSubtract(player.position)
            selfDivide(2f)
        }
        onScreen.x > -window.widthInTiles && onScreen.y > -window.heightInTiles
            && onScreen.x < window.widthInTiles
            && onScreen.y < window.heightInTiles
    }

    val jetBrainsMono by ttf("/fonts/JetBrains_mono.ttf")

    override fun SceneChildrenBuilder.build() {
        camera2DLayer(player.position) {
            postprocessing("/shaders/filter.fsh", 1) {
                shader {
                    "millis" set Global.millis().toFloat()
                }
                - chunkMap
                - player
            }
            - camera
            - ClientNode(this@World)
        }
        uiLayer {
            - FpsCounter(
                18f,
                Vec2f(.5f, .5f),
                jetBrainsMono
            )
        }
    }

    init {
        chunkMap += EntityNode(Vec2f.zero())
        chunkMap += EntityNode(Vec2f(3f, 2f))
        chunkMap += EntityNode(Vec2f(5f, 12f))
    }

    fun initWorld(sizeInChunks: Int) {

    }

    fun onChunkReceived(packet: Packet) {
        val x = packet.tokens[1].toInt()
        val y = packet.tokens[2].toInt()
        var isChunkNew = false
    }
}