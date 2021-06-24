package io.posidon.uranium.gfx.platform.opengl.assets

import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.util.Heap
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import java.nio.FloatBuffer
import java.nio.IntBuffer

inline class OpenGLMesh(
    val memory: IntBuffer
) : Mesh {

    override val vertexCount get() = memory[1]
    override val vboCount get() = memory[2]

    override fun bind() {
        GL30.glBindVertexArray(memory[0])
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, memory[HEADER_SIZE_IN_BYTES])
    }

    override fun destroy() {
        bind()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        for (i in 0 until vboCount) {
            val vboId = memory[i + HEADER_SIZE_IN_BYTES + 1]
            GL20.glDisableVertexAttribArray(i)
            GL15.glDeleteBuffers(vboId)
        }
        GL15.glDeleteBuffers(memory[HEADER_SIZE_IN_BYTES])
        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(memory[0])
        Heap.free(memory)
    }

    companion object {
        const val HEADER_SIZE_IN_BYTES = 3

        inline fun fromAddress(address: Long): OpenGLMesh {
            val header = Heap.getIntBuffer(address, HEADER_SIZE_IN_BYTES)
            return OpenGLMesh(Heap.getIntBuffer(address, HEADER_SIZE_IN_BYTES + 1 + header[2]))
        }
    }

    class FloatVBO (
        override val size: Int,
        val floats: FloatArray
    ) : Mesh.VBO {
        override fun bind(i: Int): Int {
            var buffer: FloatBuffer? = null
            return try {
                val vboId = GL15.glGenBuffers()
                buffer = Heap.mallocFloat(floats.size)
                buffer.put(floats).flip()
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW)
                GL20.glVertexAttribPointer(i, size, GL11.GL_FLOAT, false, 0, 0)
                GL30.glEnableVertexAttribArray(i)
                vboId
            } finally {
                if (buffer != null) Heap.free(buffer)
            }
        }
    }
}