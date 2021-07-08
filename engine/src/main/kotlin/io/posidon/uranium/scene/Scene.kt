package io.posidon.uranium.scene

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.input.InputManager
import io.posidon.uranium.gfx.*
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.window.Window

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
        val children: Array<Node>,
        val onInit: Layer.() -> Unit,
        val onDestroy: Layer.() -> Unit
    ) {
        var enabled = true

        fun init() {
            children.forEach(Node::init)
            onInit()
        }

        fun render(window: Window) {
            renderer.preRender()
            children.forEach { it.render(renderer, window) }
            renderer.postRender()
        }

        fun update(delta: Float) {
            children.forEach { it.update(delta) }
        }

        fun destroy() {
            children.forEach(Node::destroy)
            onDestroy()
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
            b.layers[it].build(this)
        }
        layers.forEach(Layer::init)
    }

    internal fun render(window: Window) {
        layers.forEach { if (it.enabled) it.render(window) }
    }

    internal fun update(delta: Float) {
        layers.forEach {if (it.enabled)  it.update(delta) }
    }

    internal fun internalDestroy() {
        layers.forEach(Layer::destroy)
    }
}
