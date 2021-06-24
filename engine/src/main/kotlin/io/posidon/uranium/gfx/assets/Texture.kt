package io.posidon.uranium.gfx.assets

import io.posidon.uranium.debug.Describable
import io.posidon.uranium.util.Format

interface Texture : Describable {

    val id: Int
    val width: Int
    val height: Int

    fun destroy()

    fun setWrap(wrap: Wrap)
    fun setMagFilter(filter: MagFilter)
    fun setMinFilter(filter: MinFilter)

    fun bind(i: Int)

    enum class Wrap {
        CLAMP,
        REPEAT,
        MIRRORED_REPEAT,
        CLAMP_TO_EDGE,
        CLAMP_TO_BORDER
    }

    enum class MagFilter {
        NEAREST,
        LINEAR,
    }

    enum class MinFilter {
        NEAREST,
        SMOOTHER_NEAREST,
        LINEAR
    }

    override fun describe(): String = """texture {
    |    id: ${Format.pointer(id)}
    |    width: $width
    |    height: $height
    |}""".trimMargin()
}