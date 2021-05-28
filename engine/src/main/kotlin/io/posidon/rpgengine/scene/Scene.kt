package io.posidon.rpgengine.scene

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.input.InputManager
import io.posidon.rpgengine.gfx.*
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

abstract class Scene : ContextInitialized<Scene>() {

    protected abstract fun SceneChildrenBuilder.build()

    lateinit var window: Window
        private set
    lateinit var input: InputManager
        private set

    lateinit var layers: Array<Layer>
        private set

    class Layer(
        val renderer: Renderer,
        val children: Array<Node>
    ) {
        fun init() {
            children.forEach(Node::init)
        }

        fun render(window: Window) {
            children.forEach { it.render(renderer, window) }
        }

        fun update(delta: Float) {
            children.forEach { it.update(delta) }
        }

        fun destroy() {
            children.forEach(Node::destroy)
        }
    }

    internal fun internalInit(
        log: MainLogger,
        context: Context,
        renderer: Renderer,
        window: Window,
        input: InputManager
    ) {
        this.window = window
        this.input = input
        super.internalInit(log, context)

        val b = SceneChildrenBuilder(renderer, window).apply { build() }
        layers = Array(b.layers.size) {
            val l = b.layers[it]
            val c = l.nodes
            Layer(
                renderer = l.renderer,
                children = Array(c.size) {
                    c[it].apply {
                        this.internalInit(log, context, input)
                    }
                }
            )
        }
        layers.forEach(Layer::init)
    }

    fun render(window: Window) {
        layers.forEach { it.render(window) }
    }

    fun update(delta: Float) {
        layers.forEach { it.update(delta) }
    }

    fun destroy() {
        layers.forEach(Layer::destroy)
    }
}
