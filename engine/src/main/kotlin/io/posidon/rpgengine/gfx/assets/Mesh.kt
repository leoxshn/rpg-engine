package io.posidon.rpgengine.gfx.assets

interface Mesh {
    val vertexCount: Int
    val vboCount: Int

    fun bind()
    fun destroy()

    interface VBO {
        val size: Int
        fun bind(i: Int): Int
    }
}