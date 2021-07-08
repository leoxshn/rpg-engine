package io.posidon.uranium.gfx.platform.opengl

import io.posidon.uranium.mathlib.types.Mat4f
import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.gfx.assets.Texture
import io.posidon.uranium.gfx.assets.Shader
import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.platform.opengl.assets.OpenGLTexture
import io.posidon.uranium.gfx.renderer.FrameBuffer
import io.posidon.uranium.tools.Filter
import io.posidon.uranium.window.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.*
import org.lwjgl.system.Platform
import java.nio.ByteBuffer

internal class OpenGLRenderer : Renderer {

    lateinit var QUAD: Mesh private set

    override fun preWindowInit() {
        GLFW.glfwDefaultWindowHints()

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE)

        GLFW.glfwWindowHint(GLFW.GLFW_SRGB_CAPABLE, GLFW.GLFW_TRUE)

        // Antialiasing
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 4)
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4)

        if (Platform.get() === Platform.MACOSX) {
            GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE)
        }
    }

    private lateinit var currentFrameBuffer: FrameBuffer

    override fun init(log: MainLogger, window: Window) {
        GLFW.glfwMakeContextCurrent(window.id)
        GL.createCapabilities()
        GLFW.glfwSwapInterval(0)
        GL11C.glEnable(GL11.GL_BLEND)
        GL11C.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL13C.glActiveTexture(GL13.GL_TEXTURE0)
        log.verbose?.i("Created renderer: OpenGL")
        currentFrameBuffer = window
        QUAD = OpenGLContext.makeMesh(intArrayOf(0, 1, 3, 3, 1, 2), OpenGLContext.makeVBO(
            size = 2,
            -1f, 1f,
            -1f, -1f,
            1f, -1f,
            1f, 1f
        ))
        log.verbose?.i("Created QUAD mesh")
    }

    override fun onWindowResize(width: Int, height: Int) {
        GL11.glViewport(0, 0, width, height)
    }

    override fun setClearColor(r: Float, g: Float, b: Float, a: Float) = GL11C.glClearColor(r, g, b, a)

    override fun bind(vararg textures: Texture?) {
        for (i in textures.indices) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[i]?.id ?: 0)
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
    }

    override fun renderQuad(window: Window, shader: Shader, x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
        renderQuad(window, shader, Mat4f.identity())
    }

    override fun renderMesh(mesh: Mesh, window: Window, shader: Shader, x: Float, y: Float, z: Float, scaleX: Float, scaleY: Float, scaleZ: Float, rotationX: Float, rotationY: Float, rotationZ: Float) {
        renderMesh(mesh, window, shader, Mat4f.identity())
    }

    override fun renderQuad(window: Window, shader: Shader, transform: Mat4f) {
        QUAD.bind()
        shader {
            "_engine_transofm_matix" set transform
        }
        GL11C.glDrawElements(GL11.GL_TRIANGLES, QUAD.vertexCount, GL11.GL_UNSIGNED_INT, 0)
    }

    override fun renderMesh(mesh: Mesh, window: Window, shader: Shader, transform: Mat4f) {
        mesh.bind()
        shader {
            "_engine_transofm_matix" set transform
        }
        GL11C.glDrawElements(GL11.GL_TRIANGLES, mesh.vertexCount, GL11.GL_UNSIGNED_INT, 0)
    }

    override fun renderScreen(window: Window, shader: Shader) {
        QUAD.bind()
        shader.bind()
        GL11C.glDrawElements(GL11.GL_TRIANGLES, QUAD.vertexCount, GL11.GL_UNSIGNED_INT, 0)
    }

    override fun clear() {
        GL11C.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    override fun preRender() {

    }

    override fun postRender() {

    }

    override fun destroy() {
        QUAD.destroy()
        GL20.glUseProgram(0)
        GL.destroy()
    }

    override fun useFrameBuffer(buffer: Filter, block: Renderer.() -> Unit) {
        val last = currentFrameBuffer
        currentFrameBuffer = buffer
        buffer.bind()
        clear()
        block()
        currentFrameBuffer = last
        last.bind()
    }

    override fun enable(feature: Renderer.Feature) {
        GL11.glEnable(native(feature))
    }

    override fun disable(feature: Renderer.Feature) {
        GL11.glDisable(native(feature))
    }

    fun native(feature: Renderer.Feature): Int = when (feature) {
        Renderer.Feature.DEPTH_TEST -> GL11.GL_DEPTH_TEST
    }

    override fun createColorBuffer(attachment: Int, width: Int, height: Int): Renderer.Buffer =
        ColorBuffer(GL30.GL_COLOR_ATTACHMENT0 + attachment, width, height)

    override fun createDepthBuffer(width: Int, height: Int): Renderer.Buffer =
        DepthBuffer(width, height)

    class ColorBuffer(val colorAttachment: Int, width: Int, height: Int) : Renderer.Buffer(width, height) {

        override var texture: OpenGLTexture? = null

        override fun init() {
            texture = createTextureAttachment(width, height, colorAttachment)
        }

        override fun onWindowResized() {
            texture!!.bind(0)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        private inline fun createTextureAttachment(width: Int, height: Int, colorAttachment: Int): OpenGLTexture {
            val id = GL11.glGenTextures()
            val texture = OpenGLTexture(id, width, height)
            texture.bind(0)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null as ByteBuffer?)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            texture.setWrap(Texture.Wrap.CLAMP_TO_EDGE)
            GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, colorAttachment, id, 0)
            return texture
        }
    }

    class DepthBuffer(width: Int, height: Int) : Renderer.Buffer(width, height) {

        override var texture: OpenGLTexture? = null

        override fun init() {
            texture = createDepthTextureAttachment(width, height)
        }

        override fun onWindowResized() {
            texture!!.bind(0)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, null as ByteBuffer?)
        }

        private inline fun createDepthTextureAttachment(width: Int, height: Int): OpenGLTexture {
            val id = GL11.glGenTextures()
            val texture = OpenGLTexture(id, width, height)
            texture.bind(0)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, null as ByteBuffer?)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            texture.setWrap(Texture.Wrap.CLAMP_TO_EDGE)
            GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, id, 0)
            return texture
        }
    }
}