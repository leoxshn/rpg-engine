package io.posidon.rpg

import io.posidon.rpgengine.Application
import io.posidon.rpgengine.gfx.renderer.setClearColor
import io.posidon.rpgengine.scene.Scene

class Game : Application() {

    override val scene: Scene = TileEditorScene()

    override fun createWindow(args: Array<String>) = window(
        width = 800,
        height = 560,
        title = "editor"
    )

    override fun init(args: Array<String>) {
        renderer.setClearColor(.2f, .2f, .2f)
    }
}

fun main(args: Array<String>) = Game().start(args)