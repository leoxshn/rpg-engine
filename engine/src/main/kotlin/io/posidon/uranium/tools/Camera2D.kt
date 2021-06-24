package io.posidon.uranium.tools

import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.QuadShader
import io.posidon.uranium.gfx.renderer.ModifiedRenderer
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.mathlib.types.Mat4f
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.window.Window

class Camera2D(
    renderer: Renderer,
    val followedPosition: Vec2f
) : Node() {

    val renderer = object : ModifiedRenderer {
        override val renderer = renderer
        override fun renderQuad(window: Window, quadShader: QuadShader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
            val h = 12f
            val w = h / window.height * window.width
            renderer.renderQuad(
                window,
                quadShader,
                createTransformMatrix(
                    x - xy.x,
                    y - xy.y,
                    z,
                    width,
                    height,
                    depth,
                    rotationX,
                    rotationY,
                    rotationZ,
                    1f / w,
                    1f / h
                )
            )
        }
        override fun renderMesh(mesh: Mesh, window: Window, shader: QuadShader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
            val h = 12f
            val w = h / window.height * window.width
            renderer.renderMesh(
                mesh,
                window,
                shader,
                createTransformMatrix(
                    x - xy.x,
                    y - xy.y,
                    z,
                    scaleX,
                    scaleY,
                    scaleZ,
                    rotationX,
                    rotationY,
                    rotationZ,
                    1f / w,
                    1f / h
                )
            )
        }
    }

    val xy = Vec2f.zero()
    var speed = 2f
    var acceleration = 4f

    val velocity = Vec2f.zero()
    override fun update(delta: Float) {
        val distance = followedPosition - xy
        val newVelocity = distance * delta * speed
        velocity.selfMix(newVelocity, (delta * acceleration).coerceAtMost(1f))
        xy.selfAdd(velocity)
    }

    companion object {
        internal fun createTransformMatrix(
            x: Float,
            y: Float,
            z: Float,
            width: Float,
            height: Float,
            depth: Float,
            rotationX: Float,
            rotationY: Float,
            rotationZ: Float,
            postScaleX: Float,
            postScaleY: Float
        ): Mat4f {
            val rotX = Mat4f.rotateX(rotationX.toDouble())
            val rotY = Mat4f.rotateY(rotationY.toDouble())
            val rotZ = Mat4f.rotateZ(rotationZ.toDouble())
            return Mat4f.scale(width, height, depth) * (rotX * rotY * rotZ) * Mat4f.translate(x, y, z) * Mat4f.scale(postScaleX, postScaleY, 1f)
        }
    }
}
