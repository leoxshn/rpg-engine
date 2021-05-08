package io.posidon.rpgengine.gfx.platform.opengl.assets

import io.posidon.rpgengine.gfx.assets.Shader
import io.posidon.game.shared.types.Mat4f
import io.posidon.game.shared.types.Vec2f
import io.posidon.game.shared.types.Vec3f
import io.posidon.game.shared.types.Vec3i
import io.posidon.rpgengine.util.Stack
import org.lwjgl.opengl.GL20C

class OpenGLShader(
    private val programID: Int,
    private val vertexID: Int,
    private val fragmentID: Int
) : Shader {

    private inline fun getUniformLocation(name: String) = GL20C.glGetUniformLocation(programID, name)

    override operator fun set(name: String, value: Float) = GL20C.glUniform1f(getUniformLocation(name), value)
    override operator fun set(name: String, value: Int) = GL20C.glUniform1i(getUniformLocation(name), value)
    override operator fun set(name: String, value: Boolean) = GL20C.glUniform1i(getUniformLocation(name), if (value) 1 else 0)
    override operator fun set(name: String, value: Vec2f) = GL20C.glUniform2f(getUniformLocation(name), value.x, value.y)
    override operator fun set(name: String, value: Vec3f) = GL20C.glUniform3f(getUniformLocation(name), value.x, value.y, value.z)
    override operator fun set(name: String, value: Vec3i) = GL20C.glUniform3i(getUniformLocation(name), value.x, value.y, value.z)

    override operator fun set(name: String, value: Mat4f) {
        Stack.push { stack ->
            val matrix = stack.mallocFloat(Mat4f.SIZE * Mat4f.SIZE)
            matrix.put(value.all).flip()
            GL20C.glUniformMatrix4fv(getUniformLocation(name), true, matrix)
        }
    }

    override fun bind() = GL20C.glUseProgram(programID)

    override fun destroy() {
        GL20C.glDetachShader(programID, vertexID)
        GL20C.glDetachShader(programID, fragmentID)
        GL20C.glDeleteShader(vertexID)
        GL20C.glDeleteShader(fragmentID)
        GL20C.glDeleteProgram(programID)
    }
}