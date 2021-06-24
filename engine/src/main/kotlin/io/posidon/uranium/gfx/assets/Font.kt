package io.posidon.uranium.gfx.assets

import io.posidon.uranium.util.Heap
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import java.nio.ByteBuffer

class Font(
    val texture: Texture,
    val info: STBTTFontinfo,
    val ascent: Int,
    val descent: Int,
    val lineGap: Int,
    val charData: STBTTBakedChar.Buffer,
    private val ttfBuffer: ByteBuffer
) {
    fun destroy() {
        Heap.free(ttfBuffer)
        texture.destroy()
        info.free()
        charData.free()
    }

    internal companion object {
        const val BITMAP_WIDTH = 1024
        const val BITMAP_HEIGHT = 1024
        const val BITMAP_PX_HEIGHT = 64f
    }
}