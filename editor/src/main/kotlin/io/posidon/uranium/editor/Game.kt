package io.posidon.uranium.editor

import io.posidon.uranium.Application
import io.posidon.uranium.gfx.renderer.setClearColor
import io.posidon.uranium.scene.Scene

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