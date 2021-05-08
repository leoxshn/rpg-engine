package io.posidon.rpgengine.scene.node

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.window.Window

abstract class Node {
    lateinit var log: MainLogger
        private set
    lateinit var context: Context
        private set

    internal open fun internalInit(
        log: MainLogger,
        context: Context
    ) {
        this.log = log
        this.context = context
    }

    open fun init() {}
    open fun render(renderer: Renderer, window: Window) {}
    open fun update(delta: Float) {}
    open fun destroy() {}
}