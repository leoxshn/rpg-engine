package io.posidon.rpgengine

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.events.InputManager
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.getContext
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.Scene
import io.posidon.rpgengine.window.Window
import io.posidon.rpgengine.window.WindowCreationData
import kotlinx.coroutines.*

abstract class Application {

    lateinit var log: MainLogger
        private set

    val context: Context = getContext()
    val renderer: Renderer = context.getRenderer()
    val window: Window = Window(renderer)
    val input: InputManager = InputManager()

    abstract val scene: Scene

    protected abstract fun createWindow(args: Array<String>): WindowCreationData
    protected abstract fun init(args: Array<String>)

    fun start(args: Array<String>) {
        Window.init()
        log = MainLogger(args.contains("-verbose"))
        renderer.preWindowInit()
        val (width, height, title) = createWindow(args)
        window.init(log, width, height, title, input)
        renderer.init(log, window)
        init(args)
        scene.internalInit(log, context, renderer, window, input)
        var running = true
        GlobalScope.launch {
            val updateInterval = 0.001f
            var lastTime = System.nanoTime()
            var delta = 0f
            while (running) {
                val now = System.nanoTime()
                delta += (now - lastTime) / 1000000000f
                lastTime = now
                if (delta >= updateInterval) {
                    scene.update(delta)
                    delta = 0f
                }
            }
        }
        while (!window.shouldClose) {
            window.pollEvents()
            renderer.preRender()
            scene.render(window)
            renderer.postRender()
            window.swapBuffers()
        }
        running = false
        scene.destroy()
        renderer.destroy()
        window.destroy()
    }

    fun window(
        width: Int,
        height: Int,
        title: String? = null
    ) = WindowCreationData(width, height, title)
}