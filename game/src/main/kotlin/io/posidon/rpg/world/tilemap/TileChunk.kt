package io.posidon.rpg.world.tilemap

import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.window.Window

class TileChunk(
    val chunkX: Int,
    val chunkY: Int
) {

    val levels = arrayOfNulls<TileLevel>(HEIGHT)

    fun render(renderer: Renderer, window: Window, assets: TileMap.Assets) {
        val x = chunkX * SIZE.toFloat()
        val y = chunkY * SIZE.toFloat()
        for (level in levels) {
            level?.render(renderer, window, assets, x, y)
        }
    }

    companion object {
        const val SIZE = 16
        const val HEIGHT = 16
        const val AREA = SIZE * SIZE
        const val VOLUME = AREA * HEIGHT
    }
}