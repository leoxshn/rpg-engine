package io.posidon.rpgengine.gfx.renderer

import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.window.Window

class UIRenderer(
    override val renderer: Renderer,
) : ModifiedRenderer {

    override fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
        renderer.renderQuad(window, quadShader, x - window.widthInTiles, y + window.heightInTiles, z, width, height, depth, rotationX, rotationY, rotationZ)
    }
    override fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
        renderer.renderMesh(mesh, window, shader, x - window.widthInTiles, y + window.heightInTiles, z, scaleX, scaleY, scaleZ, rotationX, rotationY, rotationZ)
    }
}