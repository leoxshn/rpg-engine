package io.posidon.uranium.tools

import io.posidon.uranium.mathlib.types.Vec2i
import io.posidon.uranium.gfx.assets.Uniforms
import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.renderer.FrameBuffer
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.scene.node.container.NodeGroup
import io.posidon.uranium.window.Window
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import java.util.*

class Filter internal constructor(
    renderer: Renderer,
    val window: Window,
    fragmentPath: String,
    colorBufferCount: Int,
    val minWidth: Int,
    val uniforms: Uniforms.() -> Unit,
    nodes: LinkedList<Node>
) : NodeGroup(nodes), FrameBuffer {

    private val shader by screenShader(fragmentPath)

    private val id = createFrameBuffer()

    private val attachments = run {
        val (w, h) = calculateBufferSize(window.width, window.height, minWidth)
        Array(colorBufferCount + 1) {
            if (it < colorBufferCount) {
                renderer.createColorBuffer(it, w, h)
            } else renderer.createDepthBuffer(w, h)
        }.apply {
            forEach(Renderer.Buffer::init)
            GL20.glDrawBuffers(IntArray(colorBufferCount) {
                GL30.GL_COLOR_ATTACHMENT0 + it
            })
        }
    }

    private val resizeListener = { _: Window, width: Int, height: Int ->
        attachments.forEach {
            val (w, h) = calculateBufferSize(width, height, minWidth)
            it.resize(w, h)
        }
    }

    override fun init() {
        super.init()
        window.addResizeListener(resizeListener)
    }

    private fun createFrameBuffer(): Int {
        val buffer = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, buffer)
        return buffer
    }

    override fun render(renderer: Renderer, window: Window) {
        renderer.useFrameBuffer(this) {
            super.render(renderer, window)
        }
        attachments.forEachIndexed { i, b ->
            b.texture!!.bind(i)
        }
        shader(uniforms)
        renderer.renderScreen(window, shader)
    }

    override fun bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id)
        GL11.glViewport(0, 0, attachments[0].width, attachments[0].height)
    }

    override fun destroy() {
        window.removeResizeListener(resizeListener)
    }

    companion object {
        fun calculateBufferSize(windowWidth: Int, windowHeight: Int, minWidth: Int): Vec2i {
            val w: Int
            val h: Int
            if (windowWidth > windowHeight) {
                h = minWidth
                w = minWidth * windowWidth / windowHeight
            } else {
                w = minWidth
                h = minWidth * windowHeight / windowWidth
            }
            return Vec2i(w, h)
        }
    }
}
