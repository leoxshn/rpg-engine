package io.posidon.uranium.gfx.platform.opengl

import io.posidon.uranium.gfx.assets.Texture
import io.posidon.uranium.gfx.platform.opengl.assets.OpenGLTexture
import io.posidon.uranium.gfx.renderer.Renderer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32
import java.nio.ByteBuffer

class DepthFrameBuffer(width: Int, height: Int) : Renderer.FrameBuffer(width, height) {

    override var texture: OpenGLTexture? = null

    override fun init() {
        texture = createDepthTextureAttachment(width, height)
    }

    override fun onWindowResized() {
        texture!!.bind(0)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL14.GL_DEPTH_COMPONENT32,
            width,
            height,
            0,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_FLOAT,
            null as ByteBuffer?
        )
    }

    private inline fun createDepthTextureAttachment(width: Int, height: Int): OpenGLTexture {
        val id = GL11.glGenTextures()
        val texture = OpenGLTexture(id, width, height)
        texture.bind(0)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL14.GL_DEPTH_COMPONENT32,
            width,
            height,
            0,
            GL11.GL_DEPTH_COMPONENT,
            GL11.GL_FLOAT,
            null as ByteBuffer?
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        texture.setWrap(Texture.Wrap.CLAMP_TO_EDGE)
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, id, 0)
        return texture
    }
}