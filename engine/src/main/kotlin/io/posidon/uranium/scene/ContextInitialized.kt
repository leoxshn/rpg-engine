package io.posidon.uranium.scene

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.gfx.Context
import io.posidon.uranium.gfx.QuadShader
import io.posidon.uranium.gfx.assets.Font
import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.assets.Shader
import io.posidon.uranium.gfx.loadQuadShader
import io.posidon.uranium.gfx.loadScreenShader
import kotlin.reflect.KProperty

abstract class ContextInitialized <SELF : ContextInitialized<SELF>> {

    lateinit var log: MainLogger
        private set
    lateinit var context: Context
        private set

    fun quadShader(fragmentPath: String): Lazy<QuadShader> = onInit { context.loadQuadShader(log, fragmentPath) }
    fun screenShader(fragmentPath: String): Lazy<Shader> = onInit { context.loadScreenShader(log, fragmentPath) }
    fun customShader(vertexPath: String, fragmentPath: String): Lazy<Shader> = onInit { context.loadShader(log, vertexPath, fragmentPath) }
    fun ttf(path: String): Lazy<Font> = onInit { context.loadTTF(log, path) }

    fun mesh(vararg indices: Int, block: Mesh.Creator.() -> Unit): Lazy<Mesh> = onInit {
        val creator = Mesh.Creator(context)
        block(creator)
        context.makeMesh(indices, *creator.vbos.toTypedArray())
    }

    fun onRenderThread(function: () -> Unit) {
        context.runOnRenderThread(function)
    }

    fun <T : Any> onInit(block: SELF.() -> T): Lazy<T> {
        return OnReadyProperty(this, block).also { onInitValues.add(it) }
    }

    fun <T : Any> onInitMutable(block: SELF.() -> T): MutableLazy<T> {
        return MutableOnReadyProperty(this, block).also { onInitValues.add(it) }
    }

    interface MutableLazy <T> : Lazy<T> {
        operator fun setValue(node: ContextInitialized<*>, property: KProperty<*>, value: T)
    }

    private val onInitValues = ArrayList<OnReadyProperty<*, *>>()
    private open class OnReadyProperty <T : Any, C : ContextInitialized<C>> (val contextInitialized: ContextInitialized<C>, val block: C.() -> T) : Lazy<T> {
        override lateinit var value: T
        private var isInitialized = false
        fun init() {
            value = block(contextInitialized as C)
            isInitialized = true
        }
        override fun isInitialized() = isInitialized
    }

    private class MutableOnReadyProperty <T : Any, C : ContextInitialized<C>> (contextInitialized: ContextInitialized<C>, block: C.() -> T) : OnReadyProperty<T, C>(contextInitialized, block), MutableLazy<T> {
        override operator fun setValue(node: ContextInitialized<*>, property: KProperty<*>, value: T) {
            node.onInitValues.remove(this)
            this.value = value
        }
    }

    internal open fun internalInit(
        log: MainLogger,
        context: Context
    ) {
        this.log = log
        this.context = context
        onInitValues.forEach { it.init() }
        onInitValues.clear()
    }
}