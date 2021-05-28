package io.posidon.rpgengine.scene.node

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.input.InputManager
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.ContextInitialized
import io.posidon.rpgengine.window.Window

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