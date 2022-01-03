package io.posidon.uranium.gfx.assets

import io.posidon.uranium.debug.Describable
import io.posidon.uranium.util.Format

interface TilesetTexture : Describable {

    val id: Int
    val width: Int
    val height: Int
    val depth: Int

    fun destroy()

    fun setWrap(wrap: Texture.Wrap)
    fun setMagFilter(filter: Texture.MagFilter)
    fun setMinFilter(filter: Texture.MinFilter)

    fun bind(i: Int)

    override fun describe(): String = """texture[] {
    |    id: ${Format.pointer(id)}
    |    width: $width
    |    height: $height
    |    depth: $depth
    |}""".trimMargin()
}