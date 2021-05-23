package io.posidon.rpg.world.tilemap

import io.posidon.game.netApi.world.Block
import io.posidon.game.shared.types.Vec2f
import io.posidon.game.shared.types.Vec2i
import io.posidon.rpg.world.tile.Tileset
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.window.Window

class TileLevel {
    private val blocks = arrayOfNulls<Block>(TileChunk.AREA)

    inline operator fun get(pos: Vec2i) = get(pos.x, pos.y)
    inline operator fun get(x: Int, y: Int): Block? = get(x * TileChunk.SIZE + y)
    operator fun get(i: Int): Block? = blocks[i]

    inline operator fun set(pos: Vec2i, block: Block?) = set(pos.x, pos.y, block)
    inline operator fun set(x: Int, y: Int, block: Block?) = set(x * TileChunk.SIZE + y, block)
    operator fun set(i: Int, block: Block?) { blocks[i] = block }

    fun render(renderer: Renderer, window: Window, assets: TileMap.Assets, x: Float, y: Float) {
        if (cliffUVnPositions != null) {
            val shader = assets.shader
            val tileset = assets.cliffTileset
            shader.bind()
            tileset.texture.bind(0)
            shader["tile_to_sheet_ratio"] = Vec2f(tileset.meta.width.toFloat() / tileset.texture.width, tileset.meta.height.toFloat() / tileset.texture.height)
            cliffUVnPositions!!.forEach {
                shader["tile_uv"] = it.first
                it.second.forEach {
                    renderer.renderQuad(window, shader, x + it.x * 2f, y + it.y * 2f, 1f, 1f)
                }
            }
        }
    }

    private var cliffUVnPositions: Array<Pair<Vec2f, Array<Vec2i>>>? = null

    fun generateCliff(tileset: Tileset, h: Int, getTile: (x: Int, y: Int, h: Int) -> Block?) {
        val map = HashMap<Vec2i, ArrayList<Vec2i>>()
        for (x in 0 until TileChunk.SIZE) {
            for (y in 0 until TileChunk.SIZE) {
                if (get(x, y) != null) {
                    val b = getBitmask(x, y, h, getTile)
                    val bitmasks = tileset.meta.bitmasks
                    map.getOrPut(bitmasks[b] ?: bitmasks[0b111_111_111] ?: Vec2i.zero()) { arrayListOf() }.add(Vec2i(x, y))
                }
            }
        }
        cliffUVnPositions = map.map { it.key.toVec2f() to it.value.toTypedArray() }.toTypedArray()
    }

    private inline fun getBitmask(x: Int, y: Int, h: Int, getTile: (x: Int, y: Int, h: Int) -> Block?): Int {
        var bitmask = 0b000_010_000
        if (getTile(x, y + 1, h) != null) bitmask = bitmask or 0b010_000_000
        if (getTile(x, y - 1, h) != null) bitmask = bitmask or 0b000_000_010
        if (getTile(x + 1, y + 1, h) != null) bitmask = bitmask or 0b001_000_000
        if (getTile(x + 1, y, h) != null) bitmask = bitmask or 0b000_001_000
        if (getTile(x + 1, y - 1, h) != null) bitmask = bitmask or 0b000_000_001
        if (getTile(x - 1, y + 1, h) != null) bitmask = bitmask or 0b100_000_000
        if (getTile(x - 1, y, h) != null) bitmask = bitmask or 0b000_100_000
        if (getTile(x - 1, y - 1, h) != null) bitmask = bitmask or 0b000_000_100
        return bitmask
    }
}