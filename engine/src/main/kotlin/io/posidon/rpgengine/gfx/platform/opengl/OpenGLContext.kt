package io.posidon.rpgengine.gfx.platform.opengl

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.debug.i
import io.posidon.game.shared.Resources
import io.posidon.rpgengine.gfx.*
import io.posidon.rpgengine.gfx.assets.Mesh
import io.posidon.rpgengine.gfx.assets.Shader
import io.posidon.rpgengine.gfx.assets.Texture
import io.posidon.rpgengine.gfx.platform.opengl.assets.OpenGLMesh
import io.posidon.rpgengine.gfx.platform.opengl.assets.OpenGLShader
import io.posidon.rpgengine.gfx.platform.opengl.assets.OpenGLTexture
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.util.Heap
import io.posidon.rpgengine.util.Stack
import io.posidon.rpgengine.util.set
import org.lwjgl.opengl.*
import org.lwjgl.stb.STBImage
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.math.min


object OpenGLContext : Context {
    override fun getRenderer(): Renderer = OpenGLRenderer()

    override fun loadTexture(log: MainLogger, path: String): Texture {
        var buf: ByteBuffer?
        val width: Int
        val height: Int
        val realPath = Resources.getRealPath(path)
        Stack.push { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)
            buf = STBImage.stbi_load(realPath, w, h, channels, 4)
            if (buf == null) throw FileNotFoundException("Texture not loaded: [" + realPath + "] " + STBImage.stbi_failure_reason())
            width = w.get()
            height = h.get()
        }
        val id = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        //GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -1f)
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE)
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            val amount = min(4f, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT))
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount)
        } else {
            log.e("error: Anisotropic filtering isn't supported")
        }
        STBImage.stbi_image_free(buf!!)
        return OpenGLTexture(id, width, height).also { log.verbose?.i("Loaded", it) }
    }

    override fun loadShader(log: MainLogger, fragmentPath: String, vertexPath: String): Shader {
        val libFile = Resources.loadAsString("/shaders/_include.glsl").trimEnd('\n', ' ', '\t') + '\n'
        val vertexFile = libFile + Resources.loadAsString(vertexPath)
        val fragmentFile = libFile + Resources.loadAsString(fragmentPath)

        val programID = GL20C.glCreateProgram()
        val vertexID = GL20C.glCreateShader(GL20C.GL_VERTEX_SHADER)
        GL20C.glShaderSource(vertexID, vertexFile)
        GL20C.glCompileShader(vertexID)
        log.verbose?.i("Compiling shader ($vertexPath, $fragmentPath)")
        if (GL20C.glGetShaderi(vertexID, GL20C.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("[SHADER ERROR | $vertexPath]: " + GL20C.glGetShaderInfoLog(vertexID))
        }
        log.verbose?.i("\t$vertexPath compiled successfully")
        val fragmentID = GL20C.glCreateShader(GL20C.GL_FRAGMENT_SHADER)
        GL20C.glShaderSource(fragmentID, fragmentFile)
        GL20C.glCompileShader(fragmentID)
        if (GL20C.glGetShaderi(fragmentID, GL20C.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("[SHADER ERROR | $fragmentPath]: " + GL20C.glGetShaderInfoLog(fragmentID))
        }
        log.verbose?.i("\t$fragmentPath compiled successfully")
        GL20C.glAttachShader(programID, vertexID)
        GL20C.glAttachShader(programID, fragmentID)
        GL20C.glLinkProgram(programID)
        if (GL20C.glGetProgrami(programID, GL20C.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("[SHADER ERROR - Linking]: " + GL20C.glGetProgramInfoLog(programID))
        }
        log.verbose?.i("\tShader linked successfully")
        GL20C.glValidateProgram(programID)
        if (GL20C.glGetProgrami(programID, GL20C.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("[SHADER ERROR - Validation]: " + GL20C.glGetProgramInfoLog(programID))
        }
        log.verbose?.i("\tShader is valid")
        return OpenGLShader(programID, vertexID, fragmentID)
    }

    override fun makeMesh(indices: IntArray, vararg vbos: Mesh.VBO): Mesh {
        val memory = Heap.mallocInt(OpenGLMesh.HEADER_SIZE_IN_BYTES + 1 + vbos.size)
        var indicesBuffer: IntBuffer? = null
        try {
            memory[0] = GL30.glGenVertexArrays()
            memory[1] = indices.size
            memory[2] = vbos.size
            GL30.glBindVertexArray(memory[0])

            // Index VBO
            val vboId = GL15.glGenBuffers()
            memory[OpenGLMesh.HEADER_SIZE_IN_BYTES] = vboId
            indicesBuffer = Heap.mallocInt(indices.size)
            indicesBuffer.put(indices).flip()
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId)
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW)

            for (i in vbos.indices) {
                memory[OpenGLMesh.HEADER_SIZE_IN_BYTES + 1 + i] = vbos[i].bind(i)
            }
        } finally {
            if (indicesBuffer != null) Heap.free(indicesBuffer)
        }
        return OpenGLMesh(memory)
    }

    override fun makeVBO(size: Int, vararg floats: Float): Mesh.VBO = OpenGLMesh.FloatVBO(size, floats)
}