package io.posidon.uranium.scene.node.container

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.input.InputManager
import io.posidon.uranium.gfx.Context
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.window.Window

open class NodeWrapper<T : Node?> (node: T) : Node() {

    var node: T = node
        set(value) {
            field = value
            if (value != null && initialized) {
                value.internalInit(log, context, input)
                value.init()
            }
        }

    var initialized = false

    override fun internalInit(
        log: MainLogger,
        context: Context,
        input: InputManager
    ) {
        super.internalInit(log, context, input)
        node?.internalInit(log, context, input)
    }

    override fun init() {
        node?.init()
        initialized = true
    }

    override fun render(renderer: Renderer, window: Window) {
        node?.render(renderer, window)
    }

    override fun update(delta: Float) {
        node?.update(delta)
    }

    override fun destroy() {
        node?.destroy()
    }
}