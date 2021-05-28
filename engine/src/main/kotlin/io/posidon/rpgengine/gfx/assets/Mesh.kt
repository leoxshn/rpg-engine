package io.posidon.rpgengine.gfx.assets

import io.posidon.rpgengine.gfx.Context

interface Mesh {
    val vertexCount: Int
    val vboCount: Int

    fun bind()
    fun destroy()

    interface VBO {
        val size: Int
        fun bind(i: Int): Int
    }

    class Creator(val context: Context) {
        val vbos = ArrayList<Mesh.VBO>()

        fun Int.v(vararg vertices: Float) {
            vbos += context.makeVBO(this, *vertices)
        }
    }
}