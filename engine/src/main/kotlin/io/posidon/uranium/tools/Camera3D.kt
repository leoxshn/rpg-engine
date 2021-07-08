package io.posidon.uranium.tools

import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.assets.Shader
import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.renderer.ModifiedRenderer
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.mathlib.types.Mat4f
import io.posidon.uranium.mathlib.types.Vec3f
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.util.ProjectionMatrix
import io.posidon.uranium.window.Window

class Camera3D(
    renderer: Renderer,
    window: Window,
    val position: Vec3f,
    val rotation: Vec3f,
    var fov: Float
) : Node() {

    val near = 0.002f
    val far = 1000f

    private val _projectionMatrix = ProjectionMatrix(fov, window.aspectRatio, near, far)
    val projectionMatrix: Mat4f get() = _projectionMatrix
    val rotationMatrix: Mat4f = createRotation()

    val viewMatrix: Mat4f = createView()

    private fun createRotation(): Mat4f {
        val rotX = Mat4f.rotateX(rotation.x.toDouble())
        val rotY = Mat4f.rotateY(rotation.y.toDouble())
        val rotZ = Mat4f.rotateY(rotation.z.toDouble())
        return rotZ * rotY * rotX
    }

    private fun createView(): Mat4f {
        val translation = Mat4f.translate(
            -position.x,
            -position.y,
            -position.z
        )
        return translation * rotationMatrix
    }

    override fun render(renderer: Renderer, window: Window) {
        _projectionMatrix.setFovAndAspectRatio(fov, window.aspectRatio)
    }

    override fun update(delta: Float) {
        rotationMatrix.set(createRotation())
        viewMatrix.set(createView())
    }

    private fun createMatrix(
        x: Float,
        y: Float,
        z: Float,
        width: Float,
        height: Float,
        depth: Float,
        rotationX: Float,
        rotationY: Float,
        rotationZ: Float
    ): Mat4f {
        val rotX = Mat4f.rotateX(rotationX.toDouble())
        val rotY = Mat4f.rotateY(rotationY.toDouble())
        val rotZ = Mat4f.rotateZ(rotationZ.toDouble())
        val transform = Mat4f.scale(width, height, depth) * (rotX * rotY * rotZ) * Mat4f.translate(x, y, z)
        return transform * viewMatrix * _projectionMatrix
    }

    val renderer = object : ModifiedRenderer {
        override val renderer = renderer

        override fun preRender() {
            enable(Renderer.Feature.DEPTH_TEST)
        }

        override fun postRender() {
            disable(Renderer.Feature.DEPTH_TEST)
        }

        override fun renderQuad(window: Window, shader: Shader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
            renderer.renderQuad(
                window,
                shader,
                createMatrix(
                    x,
                    y,
                    z,
                    width,
                    height,
                    depth,
                    rotationX,
                    rotationY,
                    rotationZ
                )
            )
        }
        override fun renderMesh(mesh: Mesh, window: Window, shader: Shader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
            renderer.renderMesh(
                mesh,
                window,
                shader,
                createMatrix(
                    x,
                    y,
                    z,
                    scaleX,
                    scaleY,
                    scaleZ,
                    rotationX,
                    rotationY,
                    rotationZ
                )
            )
        }
    }
}
