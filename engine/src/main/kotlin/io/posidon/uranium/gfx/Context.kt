package io.posidon.uranium.gfx

import io.posidon.uranium.mathlib.types.*
import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.gfx.platform.opengl.OpenGLContext
import io.posidon.uranium.gfx.assets.Font
import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.assets.Shader
import io.posidon.uranium.gfx.assets.Texture
import io.posidon.uranium.gfx.renderer.Renderer

interface Context {
    fun getRenderer(): Renderer
    fun loadTexture(log: MainLogger, path: String): Texture
    fun loadShader(log: MainLogger, fragmentPath: String, vertexPath: String): Shader
    fun makeMesh(indices: IntArray, vararg vbos: Mesh.VBO): Mesh
    fun makeVBO(size: Int, vararg floats: Float): Mesh.VBO
    fun loadTTF(log: MainLogger, path: String): Font
    fun runOnRenderThread(function: () -> Unit)
    fun handleOnRenderFunctions()
}

internal fun getContext() = OpenGLContext

@JvmInline
value class QuadShader(val shader: Shader): Shader {
    inline fun position(position: Vec2f) { shader["_engine_quad_position"] = position }
    inline fun size(size: Vec2f) { shader["_engine_quad_size"] = size }
    override fun set(name: String, value: Float) = shader.set(name, value)
    override fun set(name: String, value: Int) = shader.set(name, value)
    override fun set(name: String, value: Boolean) = shader.set(name, value)
    override fun set(name: String, value: Vec2f) = shader.set(name, value)
    override fun set(name: String, value: Vec2i) = shader.set(name, value)
    override fun set(name: String, value: Vec3f) = shader.set(name, value)
    override fun set(name: String, value: Vec3i) = shader.set(name, value)
    override fun set(name: String, value: Vec4f) = shader.set(name, value)
    override fun set(name: String, value: Mat4f) = shader.set(name, value)
    override fun bind() = shader.bind()
    override fun destroy() = shader.destroy()
}

inline fun Context.loadQuadShader(log: MainLogger, fragmentPath: String): QuadShader = QuadShader(loadShader(log, fragmentPath, "/uraniumEngine/shaders/quad.vsh"))
inline fun Context.loadScreenShader(log: MainLogger, fragmentPath: String): Shader = loadShader(log, fragmentPath, "/uraniumEngine/shaders/fullscreen.vsh")