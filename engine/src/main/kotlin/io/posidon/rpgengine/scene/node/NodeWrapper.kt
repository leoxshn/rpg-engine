package io.posidon.rpgengine.scene.node

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.events.InputManager
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.window.Window

open class NodeWrapper<T : Node> : Node() {
    var node: T? = null
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