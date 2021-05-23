package io.posidon.rpgengine.gfx

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.gfx.platform.opengl.OpenGLContext
import io.posidon.game.shared.types.Mat4f
import io.posidon.game.shared.types.Vec2f
import io.posidon.game.shared.types.Vec3f
import io.posidon.game.shared.types.Vec3i
import io.posidon.rpgengine.gfx.assets.Font
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.gfx.assets.Shader
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.gfx.renderer.Renderer

interface Context {
    fun getRenderer(): Renderer
    fun loadTexture(log: MainLogger, path: String): Texture
    fun loadShader(log: MainLogger, fragmentPath: String, vertexPath: String): Shader
    fun makeMesh(indices: IntArray, vararg vbos: Mesh.VBO): Mesh
    fun makeVBO(size: Int, vararg floats: Float): Mesh.VBO
    fun loadTTF(log: MainLogger, path: String): Font
}

internal fun getContext() = OpenGLContext

inline class QuadShader(val shader: Shader): Shader {
    inline fun position(position: Vec2f) { shader["_engine_quad_position"] = position }
    inline fun size(size: Vec2f) { shader["_engine_quad_size"] = size }
    override fun set(name: String, value: Float) = shader.set(name, value)
    override fun set(name: String, value: Int) = shader.set(name, value)
    override fun set(name: String, value: Boolean) = shader.set(name, value)
    override fun set(name: String, value: Vec2f) = shader.set(name, value)
    override fun set(name: String, value: Vec3f) = shader.set(name, value)
    override fun set(name: String, value: Vec3i) = shader.set(name, value)
    override fun set(name: String, value: Mat4f) = shader.set(name, value)
    override fun bind() = shader.bind()
    override fun destroy() = shader.destroy()
}

inline class ScreenShader(val shader: Shader): Shader {
    override fun set(name: String, value: Float) = shader.set(name, value)
    override fun set(name: String, value: Int) = shader.set(name, value)
    override fun set(name: String, value: Boolean) = shader.set(name, value)
    override fun set(name: String, value: Vec2f) = shader.set(name, value)
    override fun set(name: String, value: Vec3f) = shader.set(name, value)
    override fun set(name: String, value: Vec3i) = shader.set(name, value)
    override fun set(name: String, value: Mat4f) = shader.set(name, value)
    override fun bind() = shader.bind()
    override fun destroy() = shader.destroy()
}

inline fun Context.loadQuadShader(log: MainLogger, fragmentPath: String): QuadShader = QuadShader(loadShader(log, fragmentPath, "/shaders/quad.vsh"))
inline fun Context.loadScreenShader(log: MainLogger, fragmentPath: String): ScreenShader = ScreenShader(loadShader(log, fragmentPath, "/shaders/fullscreen.vsh"))