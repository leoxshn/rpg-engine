package io.posidon.rpg.world

import io.posidon.game.netApi.Packet
import io.posidon.game.shared.types.Vec2f
import io.posidon.rpg.Player
import io.posidon.rpgengine.scene.Scene
import io.posidon.rpgengine.scene.SceneChildrenBuilder
import io.posidon.rpgengine.tools.Camera2D
import io.posidon.rpgengine.ui.text.Text

class World : Scene() {

    var time = 0.0
    val blockDictionary = HashMap<Int, String>()

    val player = Player()
    var tileMap = TileMapWrapper()

    override fun SceneChildrenBuilder.build() {
        val camera = Camera2D(renderer, player.position.xy)
        renderer = camera.renderer
        - tileMap
        - player
        - camera
        - ClientNode(this@World)
        - Text(
            "/fonts/JetBrains_mono.ttf",
            128f,
            "test text _ - lalala TEST TEST TEST 123456789 \n 1 5 8 1234 |~ñÑçÇ",
            Vec2f(1f, 1f)
        )
        - Text(
            "/fonts/JetBrains_mono.ttf",
            18f,
            "uranium\n123",
            Vec2f(1f, 6f)
        )
    }

    fun initWorld(sizeInChunks: Int) = tileMap.initTileMap(sizeInChunks)

    fun updateTime(new: Double) {
        time = new
    }

    fun onChunkReceived(packet: Packet) {
        val x = packet.tokens[1].toInt()
        val y = packet.tokens[2].toInt()
        val z = packet.tokens[3].toInt()

        var isChunkNew = false
        /*val chunk = get(x, y, z) ?: run {
            isChunkNew = true
            TileChunk(x, y, z)
        }

        val blocks = Compressor.decompressString(packet.string.substring(
            9 + packet.tokens[1].length + packet.tokens[2].length + packet.tokens[3].length
        ).newLineUnescape(), Constants.CHUNK_SIZE_CUBE * 8)
        var isEmpty = true
        var i = 2
        while (i < blocks.length) {
            val material = (blocks[i - 1].toInt() shl 16) or blocks[i].toInt()
            val smallI = i / 4
            chunk.setVoxel(smallI,
                if (material == -1) null
                else Voxel[Voxel.dictionary[material]!!].also { isEmpty = false })
            i += 4
        }

        if (isChunkNew)
            set(x, y, z, chunk)
        else for (j in i / 3 until Constants.CHUNK_SIZE_CUBE) {
            chunk.setVoxel(j, null)
        }
        if (!isEmpty || isChunkNew) {
            generateChunkMesh(chunk)
            //val endTime = System.currentTimeMillis()
            //println("got chunk (duration: ${endTime - startTime}, pos: $x, $y, $z)")
        } else {
            chunk.deleteMesh(context, this)
        }*/
    }

    fun onSetBlockReceived(packet: Packet) {
        val (x, y, z) = packet.parseIntCoords()
        val id = packet.tokens[4].toInt()
        //setBlock(x, y, z, blockDictionary[id]?.let { Voxel[it] })?.let { generateChunkMesh(it) }
    }
}