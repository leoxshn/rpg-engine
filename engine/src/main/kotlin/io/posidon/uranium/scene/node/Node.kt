package io.posidon.uranium.scene.node

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.input.InputManager
import io.posidon.uranium.gfx.Context
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.scene.ContextInitialized
import io.posidon.uranium.window.Window

abstract class Node : ContextInitialized<Node>() {
    lateinit var input: InputManager
        private set

    internal open fun internalInit(
        log: MainLogger,
        context: Context,
        input: InputManager
    ) {
        this.input = input
        super.internalInit(log, context)
    }

    open fun init() {}
    open fun render(renderer: Renderer, window: Window) {}
    open fun update(delta: Float) {}
    open fun destroy() {}
}