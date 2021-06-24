package io.posidon.uranium.gfx.renderer

import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.QuadShader
import io.posidon.uranium.gfx.assets.Shader
import io.posidon.uranium.gfx.assets.Texture
import io.posidon.uranium.mathlib.types.Mat4f
import io.posidon.uranium.tools.Filter
import io.posidon.uranium.window.Window

interface Renderer {

    fun preWindowInit()
    fun init(log: MainLogger, window: Window)

    fun onWindowResize(width: Int, height: Int)
    fun setClearColor(r: Float, g: Float, b: Float, a: Float)

    fun bind(vararg textures: Texture?)

    fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float)
    fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float)
    fun renderQuad(window: Window, quadShader: QuadShader, transform: Mat4f)
    fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, transform: Mat4f)
    fun renderScreen(window: Window, shader: Shader)

    fun clear()
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

inline fun Renderer.renderQuad2D(
    window: Window,
    quadShader: QuadShader,
    position: Vec2f,
    size: Vec2f,
    rotationZ: Float = 0f
) = renderQuad2D(window, quadShader, position, size.x, size.y, rotationZ)

inline fun Renderer.renderQuad2D(
    window: Window,
    quadShader: QuadShader,
    position: Vec2f,
    width: Float,
    height: Float,
    rotationZ: Float = 0f
) = renderQuad2D(window, quadShader, position.x, position.y, width, height, rotationZ)

inline fun Renderer.renderQuad2D(
    window: Window,
    quadShader: QuadShader,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    rotationZ: Float = 0f
) = renderQuad(window, quadShader, x, y, 0f, width, height, 1f, 0f, 0f, rotationZ)

inline fun Renderer.renderMesh2D(
    mesh: Mesh,
    window: Window,
    shader: QuadShader,
    position: Vec2f,
    scale: Vec2f,
    rotation: Float = 0f
) = renderMesh2D(mesh, window, shader, position, scale.x, scale.y, rotation)

inline fun Renderer.renderMesh2D(
    mesh: Mesh,
    window: Window,
    shader: QuadShader,
    position: Vec2f,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    rotation: Float = 0f
) = renderMesh2D(mesh, window, shader, position.x, position.y, scaleX, scaleY, rotation)

inline fun Renderer.renderMesh2D(
    mesh: Mesh,
    window: Window,
    shader: QuadShader,
    x: Float,
    y: Float,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    rotation: Float = 0f
) = renderMesh(mesh, window, shader, x, y, 0f, scaleX, scaleY, 1f, 0f, 0f, rotation)