package io.posidon.rpgengine.gfx.renderer

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Shader
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.tools.Filter
import io.posidon.rpgengine.window.Window

interface Renderer {

    fun preWindowInit()
    fun init(log: MainLogger, window: Window)

    fun onWindowResize(width: Int, height: Int)
    fun setClearColor(r: Float, g: Float, b: Float, a: Float)

    fun bind(vararg textures: Texture?)

    fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, width: Float, height: Float)
    fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, scaleX: Float, scaleY: Float)
    fun renderScreen(window: Window, shader: Shader)

    fun preRender()
    fun postRender()

    fun destroy()

    fun useFrameBuffer(buffer: Filter, block: Renderer.() -> Unit)

    fun createColorBuffer(
        attachment: Int,
        width: Int,
        height: Int
    ): Buffer

    fun createDepthBuffer(
        width: Int,
        height: Int
    ): Buffer

    abstract class Buffer (
        width: Int,
        height: Int
    ) {
        var width = width
            private set
        var height = height
            private set

        abstract val texture: Texture?

        abstract fun init()
        abstract fun onWindowResized()

        fun resize(width: Int, height: Int) {
            this.width = width
            this.height = height
            onWindowResized()
        }

        fun destroy() {
            texture?.destroy()
        }
    }
}

internal interface FrameBuffer {
    fun bind()
}

inline fun Renderer.setClearColor(r: Float, g: Float, b: Float) = setClearColor(r, g, b, 1f)