package io.posidon.rpg.client.world

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpg.server.Server
import io.posidon.rpgengine.Global
import io.posidon.rpgengine.scene.Scene
import io.posidon.rpgengine.scene.SceneChildrenBuilder
import io.posidon.rpgengine.scene.node.container.ChunkMap2D
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
            postprocessing("/shaders/filter.fsh", 1, minWidth = 512) {
                shader {
                    "millis" set Global.millis().toFloat()
                }
                - chunkMap
                - player
            }
            - camera
            - Server(chunkMap, player)
        }
        uiLayer {
            - FpsCounter(
                18f,
                Vec2f(.5f, .5f),
                jetBrainsMono
            )
        }
    }
}