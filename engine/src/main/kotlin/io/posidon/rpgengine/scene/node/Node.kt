package io.posidon.rpgengine.scene.node

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.events.InputManager
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.window.Window
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class Node {
    lateinit var log: MainLogger
        private set
    lateinit var context: Context
        private set
    lateinit var input: InputManager
        private set

    internal open fun internalInit(
        log: MainLogger,
        context: Context,
        input: InputManager
    ) {
        this.log = log
        this.context = context
        this.input = input
        onReadyValues.forEach { it.init() }
        onReadyValues.clear()
    }

    private val onReadyValues = ArrayList<OnReadyProperty<*>>()
    internal class OnReadyProperty<T : Any>(val block: () -> T) : Lazy<T> {
        override lateinit var value: T
        private var isInitialized = false
        fun init() {
            value = block()
            isInitialized = true
        }
        override fun isInitialized() = isInitialized
    }

    fun <T : Any> onInit(block: () -> T): Lazy<T> {
        return OnReadyProperty(block).also { onReadyValues.add(it) }
    }

    open fun init() {}
    open fun render(renderer: Renderer, window: Window) {}
    open fun update(delta: Float) {}
    open fun destroy() {}
}