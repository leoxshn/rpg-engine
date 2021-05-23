package io.posidon.rpg.world.tilemap

import io.posidon.game.netApi.world.Block
import io.posidon.game.shared.types.Vec2i
import io.posidon.rpg.world.tile.Tileset
import io.posidon.rpg.world.tile.loadTileset
import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.gfx.loadQuadShader
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

class TileMap(val sizeInChunks: Int) : Node() {

    val assets by onInit {
        Assets(
            cliffTileset = context.loadTileset(log, "/textures/tiles/dark_cobble").apply {
                texture.setMagFilter(Texture.MagFilter.NEAREST)
                texture.setMinFilter(Texture.MinFilter.NEAREST)
            },
            shader = context.loadQuadShader(log, "/shaders/tile_chunk.fsh"),
            spriteSheet = context.loadTexture(log, "/textures/player-walk.png").apply {
                setMagFilter(Texture.MagFilter.NEAREST)
                setMinFilter(Texture.MinFilter.NEAREST)
            }
        )
    }

    override fun init() {
        getChunk(0, 0)!!.levels[0]!!.generateCliff(assets.cliffTileset, 0) { x, y, h ->
            val pos = Vec2i(x, y) + Vec2i(0, 0)
            getTile(
                Math.floorMod(pos.x, sizeInChunks * TileChunk.SIZE),
                Math.floorMod(pos.y, sizeInChunks * TileChunk.SIZE), h)
        }
    }

    override fun render(renderer: Renderer, window: Window) {
        for (chunk in chunks) {
            chunk?.render(renderer, window, assets)
        }
    }

    val chunks: Array<TileChunk?> = arrayOfNulls(sizeInChunks * sizeInChunks)

    fun getChunk(y: Int, x: Int): TileChunk? = chunks[getChunkIndex(x, y)]
    fun setChunk(y: Int, x: Int, chunk: TileChunk) = chunks.set(getChunkIndex(x, y), chunk)

    fun getChunkIndex(y: Int, x: Int): Int {
        when {
            x < 0 || x >= sizeInChunks -> throw IllegalArgumentException("x = $x")
            y < 0 || y >= sizeInChunks -> throw IllegalArgumentException("y = $y")
        }
        return x * sizeInChunks + y
    }

    fun getTile(y: Int, x: Int, h: Int): Block? {
        return getChunk(
            x / TileChunk.SIZE, y / TileChunk.SIZE
        )?.levels?.get(h % TileChunk.HEIGHT)?.get(x % TileChunk.SIZE, y % TileChunk.SIZE)
    }

    class Assets(
        val cliffTileset: Tileset,
        var shader: QuadShader,
        var spriteSheet: Texture
    ) {
        fun destroy() {
            shader.destroy()
            spriteSheet.destroy()
            cliffTileset.destroy()
        }
    }

    override fun destroy() {
        assets.destroy()
    }
}