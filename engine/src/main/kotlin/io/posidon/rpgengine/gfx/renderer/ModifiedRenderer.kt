package io.posidon.rpgengine.gfx.renderer

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.window.Window

abstract class ModifiedRenderer(
    val renderer: Renderer
): Renderer {

    override fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, width: Float, height: Float) = renderer.renderQuad(window, quadShader, x, y, width, height)
    override fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, scaleX: Float, scaleY: Float) = renderer.renderMesh(mesh, window, shader, x, y, scaleX, scaleY)
    override fun preWindowInit() = renderer.preWindowInit()
    override fun init(log: MainLogger, window: Window) = renderer.init(log, window)
    override fun onWindowResize(width: Int, height: Int) = renderer.onWindowResize(width, height)
    override fun setClearColor(r: Float, g: Float, b: Float, a: Float) = renderer.setClearColor(r, g, b, a)
    override fun bind(vararg textures: Texture?) = renderer.bind(*textures)
    override fun preRender() = renderer.preRender()
    override fun postRender() = renderer.postRender()
    override fun destroy() = renderer.destroy()
}
