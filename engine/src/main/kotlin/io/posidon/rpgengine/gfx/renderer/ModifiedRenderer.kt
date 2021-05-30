package io.posidon.rpgengine.gfx.renderer

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Shader
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.tools.Filter
import io.posidon.rpgengine.window.Window

interface ModifiedRenderer : Renderer {
    val renderer: Renderer

    override fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float) = renderer.renderQuad(window, quadShader, x, y, z, width, height, depth, rotationX, rotationY, rotationZ)
    override fun renderScreen(window: Window, shader: Shader) = renderer.renderScreen(window, shader)
    override fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float) = renderer.renderMesh(mesh, window, shader, x, y, z, scaleX, scaleY, scaleZ, rotationX, rotationY, rotationZ)
    override fun preWindowInit() = renderer.preWindowInit()
    override fun init(log: MainLogger, window: Window) = renderer.init(log, window)
    override fun onWindowResize(width: Int, height: Int) = renderer.onWindowResize(width, height)
    override fun setClearColor(r: Float, g: Float, b: Float, a: Float) = renderer.setClearColor(r, g, b, a)
    override fun bind(vararg textures: Texture?) = renderer.bind(*textures)
    override fun preRender() = renderer.preRender()
    override fun postRender() = renderer.postRender()
    override fun destroy() = renderer.destroy()
    override fun useFrameBuffer(buffer: Filter, block: Renderer.() -> Unit) = renderer.useFrameBuffer(buffer, block)
    override fun createColorBuffer(attachment: Int, width: Int, height: Int): Renderer.Buffer = renderer.createColorBuffer(attachment, width, height)
    override fun createDepthBuffer(width: Int, height: Int): Renderer.Buffer = renderer.createDepthBuffer(width, height)
}
