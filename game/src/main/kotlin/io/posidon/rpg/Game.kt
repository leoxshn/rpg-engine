package io.posidon.rpg

import io.posidon.rpg.world.World
import io.posidon.rpgengine.Application
import io.posidon.rpgengine.scene.Scene
import io.posidon.rpgengine.gfx.renderer.setClearColor

class Game : Application() {

    override val scene: Scene = World()

    override fun createWindow(args: Array<String>) = window(
        width = 800,
        height = 560,
        title = "rpg"
    )

    override fun init(args: Array<String>) {
        renderer.setClearColor(.2f, .2f, .2f)
    }
}

fun main(args: Array<String>) = Game().start(args)