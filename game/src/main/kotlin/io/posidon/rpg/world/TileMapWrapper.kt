package io.posidon.rpg.world

import io.posidon.game.netApi.world.Block
import io.posidon.rpg.world.tilemap.TileChunk
import io.posidon.rpg.world.tilemap.TileLevel
import io.posidon.rpg.world.tilemap.TileMap
import io.posidon.rpgengine.scene.node.NodeWrapper

class TileMapWrapper : NodeWrapper<TileMap>() {

    init {
        node = TileMap(16)
        node!!.setChunk(0, 0, TileChunk(0, 0).apply {
            levels[0] = TileLevel().apply {
                this[0, 0] = Block.STONE
                this[0, 1] = Block.STONE
                this[0, 2] = Block.STONE
                this[1, 0] = Block.STONE
            }
        })
    }

    fun initTileMap(sizeInChunks: Int) {
        node = TileMap(sizeInChunks)
    }
}