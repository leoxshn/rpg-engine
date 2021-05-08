package io.posidon.rpgengine.scene

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.events.InputManager
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

abstract class Scene {

    protected abstract fun SceneChildrenBuilder.build()

    lateinit var log: MainLogger
        private set
    lateinit var context: Context
        private set
    lateinit var window: Window
        private set
    lateinit var input: InputManager
        private set
    lateinit var children: Array<Node>
        private set

    private lateinit var renderer: Renderer

    internal fun internalInit(
        log: MainLogger,
        context: Context,
        renderer: Renderer,
        window: Window,
        input: InputManager
    ) {
        this.log = log
        this.context = context
        this.window = window
        this.input = input

        val b = SceneChildrenBuilder(renderer).apply { build() }
        val c = b.nodes
        children = Array(c.size) {
            c[it].apply {
                this.internalInit(log, context)
            }
        }
        this.renderer = b.renderer
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
