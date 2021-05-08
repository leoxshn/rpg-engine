package io.posidon.rpgengine.gfx.platform.opengl.assets

import io.posidon.rpgengine.gfx.assets.Texture
import org.lwjgl.opengl.*

class OpenGLTexture internal constructor(
    override val id: Int,
    override val width: Int,
    override val height: Int
) : Texture {

    override fun destroy() = GL11.glDeleteTextures(id)

    override fun setWrap(wrap: Texture.Wrap) {
        bind(0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, native(wrap))
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, native(wrap))
    }

    override fun setMagFilter(filter: Texture.MagFilter) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, native(filter))
    }
    
    override fun setMinFilter(filter: Texture.MinFilter) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, native(filter))
    }

    override fun bind(i: Int) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
    }

    private inline fun native(v: Texture.Wrap) = when (v) {
        Texture.Wrap.CLAMP -> GL11.GL_CLAMP
        Texture.Wrap.REPEAT -> GL11.GL_REPEAT
        Texture.Wrap.MIRRORED_REPEAT -> GL14.GL_MIRRORED_REPEAT
        Texture.Wrap.CLAMP_TO_EDGE -> GL14.GL_CLAMP_TO_EDGE
        Texture.Wrap.CLAMP_TO_BORDER -> GL14.GL_CLAMP_TO_BORDER
    }

    private inline fun native(v: Texture.MagFilter) = when (v) {
        Texture.MagFilter.NEAREST -> GL11.GL_NEAREST
        Texture.MagFilter.LINEAR -> GL11.GL_LINEAR
    }

    private inline fun native(v: Texture.MinFilter) = when (v) {
        Texture.MinFilter.NEAREST -> GL11.GL_NEAREST_MIPMAP_NEAREST
        Texture.MinFilter.SMOOTHER_NEAREST -> GL11.GL_LINEAR_MIPMAP_NEAREST
        Texture.MinFilter.LINEAR -> GL11.GL_LINEAR_MIPMAP_LINEAR
    }
}