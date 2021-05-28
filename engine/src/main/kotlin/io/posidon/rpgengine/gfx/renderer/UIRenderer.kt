package io.posidon.rpgengine.gfx.renderer

import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.window.Window

class UIRenderer(
    override val renderer: Renderer,
) : ModifiedRenderer {

    override fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, width: Float, height: Float) {
        renderer.renderQuad(window, quadShader, x - window.widthInTiles, y + window.heightInTiles, width, height)
    }
    override fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, scaleX: Float, scaleY: Float) {
        renderer.renderMesh(mesh, window, shader, x - window.widthInTiles, y + window.heightInTiles, scaleX, scaleY)
    }
}