package io.posidon.rpg.client.world

import io.posidon.rpg.server.Server
import io.posidon.uranium.Global
import io.posidon.uranium.scene.Scene
import io.posidon.uranium.scene.SceneChildrenBuilder
import io.posidon.uranium.scene.node.container.ChunkMap2D
import io.posidon.uranium.scene.node.util.FpsCounter
import io.posidon.uranium.mathlib.types.Vec2f

class World : Scene() {

    val jetBrainsMono by ttf("/fonts/JetBrains_mono.ttf")
    val lexendDeca by ttf("/fonts/Lexend-Medium.ttf")

    override fun SceneChildrenBuilder.build() {

        val chunkMap = ChunkMap2D(32)
        val inspector = Inspector(
            Vec2f(8f, 86f),
            jetBrainsMono,
        )
        val player = Player(chunkMap, inspector)

        camera2DLayer(player.position) {
            chunkMap.setIsLoaded {
                val onScreen = it.toVec2f().apply {
                    selfMultiply(32f)
                    selfSubtract(camera.xy)
                }
                onScreen.x > -window.width - 32f
                    && onScreen.y > -window.height - 32f
                    && onScreen.x < window.width
                    && onScreen.y < window.height
            }
            - post("/shaders/postprocessing/bloom_v.fsh", 2) {
                shader {
                    "resolution" set window.size.toVec2f()
                }
                - post("/shaders/postprocessing/bloom_h.fsh", 1) {
                    shader {
                        "resolution" set window.size.toVec2f()
                    }
                    - post(
                        "/shaders/postprocessing/entity_filter.fsh",
                        1,
                        minWidth = 512) {
                        shader {
                            "millis" set Global.millis().toFloat()
                        }
                        - background("/shaders/objects/background.fsh") {
                            "millis" set Global.millis().toFloat()
                            "position" set camera.xy
                            "resolution" set resolution(window).toVec2f()
                            "height_in_tiles" set 12f
                        }
                        - chunkMap
                        - player
                    }
                    - Indicators(player)
                }
            }
            - camera
            - Server(chunkMap, player)
        }
        uiLayer {
            - FpsCounter(
                18f,
                Vec2f(8f, 8f),
                jetBrainsMono,
            )
            - inspector
        }
    }
}