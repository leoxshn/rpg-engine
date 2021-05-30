package io.posidon.rpgengine.tools

import io.posidon.game.shared.types.Vec2i
import io.posidon.rpgengine.gfx.assets.Uniforms
import io.posidon.rpgengine.gfx.assets.invoke
import io.posidon.rpgengine.gfx.loadQuadShader
import io.posidon.rpgengine.gfx.loadScreenShader
import io.posidon.rpgengine.gfx.renderer.FrameBuffer
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.scene.node.container.NodeGroup
import io.posidon.rpgengine.window.Window
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import java.util.*

class Filter internal constructor(
    renderer: Renderer,
    window: Window,
    fragmentPath: String,
    colorBufferCount: Int,
    val minWidth: Int,
    val uniforms: Uniforms.() -> Unit,
    nodes: LinkedList<Node>
) : NodeGroup(nodes), FrameBuffer {

    private val shader by onInit { context.loadScreenShader(log, fragmentPath) }

    private val id = createFrameBuffer()

    fun calculateBufferSize(window: Window, minWidth: Int): Vec2i {
        val w: Int
        val h: Int
        if (window.width > window.height) {
            h = minWidth
            w = minWidth * window.width / window.height
        } else {
            w = minWidth
            h = minWidth * window.height / window.width
        }
        return Vec2i(w, h)
    }

    private val attachments = run {
        val (w, h) = calculateBufferSize(window, minWidth)
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

    init {
        window.addResizeListener { _, width, height ->
            attachments.forEach {
                val (w, h) = calculateBufferSize(window, minWidth)
                it.resize(w, h)
            }
        }
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
}
