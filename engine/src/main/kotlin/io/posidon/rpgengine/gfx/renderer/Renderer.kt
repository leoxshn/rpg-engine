package io.posidon.rpgengine.gfx.renderer

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.window.Window

interface Renderer {

    fun preWindowInit()
    fun init(log: MainLogger, window: Window)

    fun onWindowResize(width: Int, height: Int)
    fun setClearColor(r: Float, g: Float, b: Float, a: Float)

    fun bind(vararg textures: Texture?)

    fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, width: Float, height: Float)
    fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, scaleX: Float, scaleY: Float)

    fun preRender()
    fun postRender()

    fun destroy()
}

inline fun Renderer.setClearColor(r: Float, g: Float, b: Float) = setClearColor(r, g, b, 1f)