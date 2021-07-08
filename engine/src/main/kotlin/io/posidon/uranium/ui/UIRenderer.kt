package io.posidon.uranium.ui

import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.assets.Shader
import io.posidon.uranium.gfx.renderer.ModifiedRenderer
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.tools.Camera2D
import io.posidon.uranium.window.Window

class UIRenderer(
    override val renderer: Renderer,
) : ModifiedRenderer {

    override fun renderQuad(window: Window, shader: Shader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
        renderer.renderQuad(
            window,
            shader,
            Camera2D.createTransformMatrix(
                x - window.width,
                y + window.height,
                z,
                width,
                height,
                depth,
                rotationX,
                rotationY,
                rotationZ,
                1f / window.width,
                1f / window.height
            )
        )
    }

    override fun renderMesh(mesh: Mesh, window: Window, shader: Shader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
        renderer.renderMesh(
            mesh,
            window,
            shader,
            Camera2D.createTransformMatrix(
                x - window.width,
                y + window.height,
                z,
                scaleX,
                scaleY,
                scaleZ,
                rotationX,
                rotationY,
                rotationZ,
                1f / window.width,
                1f / window.height
            )
        )
    }
}