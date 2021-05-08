package io.posidon.server.world

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class WorldSaver(
    val folderName: String
) {

    val folder = File(folderName)

    init {
        folder.mkdir()
    }

    fun saveChunk(x: Int, y: Int, chunk: Chunk) {
        FileOutputStream(File(getChunkFolder(x, y).also { it.mkdirs() }, BLOCK_DATA_FILE_NAME)).use {
            it.write(chunk.getSaveBytes())
        }
    }

    fun hasChunk(x: Int, y: Int): Boolean {
        return getChunkFolder(x, y).exists()
    }

    fun loadChunk(x: Int, y: Int): Chunk? {
        return try {
            FileInputStream(File(getChunkFolder(x, y).also { it.mkdirs() }, BLOCK_DATA_FILE_NAME)).use {
                Chunk.readFromInputStream(it)
            }
        } catch (e: IOException) {
            null
        }
    }

    inline fun getChunkFolder(x: Int, y: Int): File {
        return File(folder, generateChunkSaveName(x, y))
    }

    inline fun generateChunkSaveName(x: Int, y: Int): String {
        return "x${x.toString(16)}y${y.toString(16)}"
    }

    companion object {
        const val BLOCK_DATA_FILE_NAME = "blocks"
    }
}
