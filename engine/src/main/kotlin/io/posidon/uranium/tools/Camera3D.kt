package io.posidon.uranium.tools

import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.QuadShader
import io.posidon.uranium.gfx.renderer.ModifiedRenderer
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.mathlib.types.Mat4f
import io.posidon.uranium.mathlib.types.Vec3f
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.util.ProjectionMatrix
import io.posidon.uranium.window.Window

class Camera3D(
    renderer: Renderer,
    val position: Vec3f,
    val rotation: Vec3f,
    var fov: Float
) : Node() {

    val near = 0.002f
    val far = 100f

    private val _projectionMatrix = ProjectionMatrix(fov, 1f, near, far)
    val projectionMatrix: Mat4f get() = _projectionMatrix
    val viewMatrix: Mat4f = createView()

    fun createView(): Mat4f {
        val translation = Mat4f.identity().apply {
            this[3, 0] = -position.x
            this[3, 1] = -position.y
            this[3, 2] = -position.z
        }
        val rotX = Mat4f.rotateX(rotation.x.toDouble())
        val rotY = Mat4f.rotateY(rotation.y.toDouble())
        val rotZ = Mat4f.rotateY(rotation.z.toDouble())
        return translation * (rotZ * rotY * rotX)
    }

    override fun render(renderer: Renderer, window: Window) {
        _projectionMatrix.setFovAndAspectRatio(fov, window.aspectRatio)
    }

    override fun update(delta: Float) {
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
        override fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
            renderer.renderQuad(
                window,
                quadShader,
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
        override fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
            renderer.renderMesh(
                mesh,
                window,
                shader,
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
        }
    }
}
