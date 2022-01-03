package io.posidon.uranium.gfx

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.gfx.assets.*
import io.posidon.uranium.gfx.platform.opengl.OpenGLContext
import io.posidon.uranium.gfx.renderer.Renderer

interface Context {
    fun getRenderer(): Renderer
    fun loadTexture(log: MainLogger, path: String): Texture
    fun loadTilesetTexture(log: MainLogger, path: String, tileSize: Int): TilesetTexture
    fun loadShader(log: MainLogger, fragmentPath: String, vertexPath: String): Shader
    fun makeMesh(indices: IntArray, vararg vbos: Mesh.VBO): Mesh
    fun makeVBO(size: Int, vararg floats: Float): Mesh.VBO
    fun loadTTF(log: MainLogger, path: String): Font
    fun runOnRenderThread(function: () -> Unit)
    fun handleOnRenderFunctions()
}

internal fun getContext() = OpenGLContext

inline fun Context.loadObjectShader(log: MainLogger, fragmentPath: String): Shader = loadShader(log, fragmentPath, "/uraniumEngine/shaders/quad.vsh")
inline fun Context.loadScreenShader(log: MainLogger, fragmentPath: String): Shader = loadShader(log, fragmentPath, "/uraniumEngine/shaders/fullscreen.vsh")