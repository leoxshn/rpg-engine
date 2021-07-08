package io.posidon.uranium.gfx.platform.opengl

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.debug.i
import io.posidon.uranium.util.Resources
import io.posidon.uranium.gfx.*
import io.posidon.uranium.gfx.assets.Font
import io.posidon.uranium.gfx.assets.Mesh
import io.posidon.uranium.gfx.assets.Shader
import io.posidon.uranium.gfx.assets.Texture
import io.posidon.uranium.gfx.platform.opengl.assets.OpenGLMesh
import io.posidon.uranium.gfx.platform.opengl.assets.OpenGLShader
import io.posidon.uranium.gfx.platform.opengl.assets.OpenGLTexture
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.util.Heap
import io.posidon.uranium.util.Stack
import io.posidon.uranium.util.set
import org.lwjgl.opengl.*
import org.lwjgl.stb.STBImage
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.*
import kotlin.collections.HashMap
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.math.min


object OpenGLContext : Context {
    override fun getRenderer(): Renderer = OpenGLRenderer()

    override fun loadTexture(log: MainLogger, path: String): Texture = textureCache.getOrPut(path) {
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
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -1f)
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
        OpenGLTexture(id, width, height).also { log.verbose?.i("Loaded", it) }
    }

    private fun preprocessGLSL(filePath: String, glsl: String): String {
        return glsl.lines().map {
            if (it.startsWith("#include")) {
                val path = it.substringAfter("#include").trim()
                    .substringAfter('"')
                    .substringBeforeLast('"')
                Resources.loadAsString((Path(filePath).parent / path).toString())
            } else it
        }.joinToString("\n")
    }

    override fun loadShader(log: MainLogger, fragmentPath: String, vertexPath: String): Shader = shaderCache.getOrPut(fragmentPath to vertexPath) {
        val libPath = "/uraniumEngine/shaders/_include.glsl"
        val libFile = preprocessGLSL(libPath, Resources.loadAsString(libPath).trimEnd('\n', ' ', '\t') + '\n')
        val vertexFile = libFile + preprocessGLSL(vertexPath, Resources.loadAsString(vertexPath))
        val fragmentFile = libFile + preprocessGLSL(fragmentPath, Resources.loadAsString(fragmentPath))

        val programID = GL20C.glCreateProgram()
        val vertexID = GL20C.glCreateShader(GL20C.GL_VERTEX_SHADER)
        GL20C.glShaderSource(vertexID, vertexFile)
        GL20C.glCompileShader(vertexID)
        log.verbose?.i("Compiling shader ($vertexPath, $fragmentPath)")
        if (GL20C.glGetShaderi(vertexID, GL20C.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("[SHADER ERROR - Vertex | $vertexPath]: " + GL20C.glGetShaderInfoLog(vertexID))
        }
        log.verbose?.i("\t$vertexPath compiled successfully")
        val fragmentID = GL20C.glCreateShader(GL20C.GL_FRAGMENT_SHADER)
        GL20C.glShaderSource(fragmentID, fragmentFile)
        GL20C.glCompileShader(fragmentID)
        if (GL20C.glGetShaderi(fragmentID, GL20C.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("[SHADER ERROR - Fragment | $fragmentPath]: " + GL20C.glGetShaderInfoLog(fragmentID))
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
        OpenGLShader(programID, vertexID, fragmentID)
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

    override fun loadTTF(log: MainLogger, path: String): Font = fontCache.getOrPut(path) {
        val ttf = Resources.loadAsByteBuffer(path)
        val ttfBuffer = Heap.malloc(ttf.size).put(ttf).flip()

        val info = STBTTFontinfo.create()
        check(STBTruetype.stbtt_InitFont(info, ttfBuffer)) {
            "Failed to initialize font info"
        }

        Stack.push { stack ->
            val pAscent = stack.mallocInt(1)
            val pDescent = stack.mallocInt(1)
            val pLineGap = stack.mallocInt(1)

            STBTruetype.stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap)

            val texID = GL11.glGenTextures()
            val charData = STBTTBakedChar.malloc(1024 * 1024)

            val bitmap = Heap.malloc(Font.BITMAP_WIDTH * Font.BITMAP_HEIGHT)
            check(STBTruetype.stbtt_BakeFontBitmap(
                ttfBuffer,
                Font.BITMAP_PX_HEIGHT,
                bitmap,
                Font.BITMAP_WIDTH,
                Font.BITMAP_HEIGHT,
                32,
                charData
            ) != 0) {
                "Failed baking the bitmap"
            }

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID)
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RED,
                Font.BITMAP_WIDTH,
                Font.BITMAP_HEIGHT,
                0,
                GL11.GL_RED,
                GL11.GL_UNSIGNED_BYTE,
                bitmap
            )
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -1f)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)

            Heap.free(bitmap)

            val texture = OpenGLTexture(texID, Font.BITMAP_WIDTH, Font.BITMAP_HEIGHT)
            Font(texture, info, pAscent[0], pDescent[0], pLineGap[0], charData, ttfBuffer)
        }
    }

    private val onRenderFunctions = LinkedList<() -> Unit>()
    private val renderThread = Thread.currentThread()
    override fun runOnRenderThread(function: () -> Unit) {
        if (renderThread == Thread.currentThread()) {
            function()
        } else onRenderFunctions += function
    }

    override fun handleOnRenderFunctions() {
        while (onRenderFunctions.isNotEmpty())
            onRenderFunctions.pop()()
    }

    private val fontCache = HashMap<String, Font>()
    private val shaderCache = HashMap<Pair<String, String>, Shader>()
    private val textureCache = HashMap<String, Texture>()
}