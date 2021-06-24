package io.posidon.uranium

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.input.InputManager
import io.posidon.uranium.gfx.Context
import io.posidon.uranium.gfx.FrameSynchronizer
import io.posidon.uranium.gfx.getContext
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.scene.Scene
import io.posidon.uranium.window.Window
import io.posidon.uranium.window.WindowCreationData
import kotlinx.coroutines.*

abstract class Application {

    lateinit var log: MainLogger
        private set

    val context: Context = getContext()
    val renderer: Renderer = context.getRenderer()
    val window: Window = Window(renderer)
    val input: InputManager = InputManager()

    abstract val scene: Scene

    var targetFPS = 60

    protected abstract fun createWindow(args: Array<String>): WindowCreationData
    protected abstract fun init(args: Array<String>)

    fun window(
        width: Int,
        height: Int,
        title: String? = null
    ) = WindowCreationData(width, height, title)

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
        run {
            frameSynchronizer.init()
            while (!window.shouldClose) {
                window.pollEvents()
                renderer.clear()
                context.handleOnRenderFunctions()
                scene.render(window)
                renderer.postRender()
                window.swapBuffers()
                frameSynchronizer.sync(targetFPS)
            }
        }
        running = false
        scene.destroy()
        internalDestroy()
    }

    private fun internalDestroy() {
        destroy()
        renderer.destroy()
        window.destroy()
        input.destroy()
    }

    open fun destroy() {}

    private val frameSynchronizer = FrameSynchronizer()
}