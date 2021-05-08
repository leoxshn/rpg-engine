package io.posidon.rpgengine.window

import io.posidon.rpgengine.debug.Describable
import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.debug.i
import io.posidon.rpgengine.debug.invoke
import io.posidon.rpgengine.events.InputManager
import io.posidon.game.shared.Format
import io.posidon.rpgengine.gfx.renderer.Renderer
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback

class Window internal constructor(private val renderer: Renderer) : Describable {

    var heightInTiles: Float = 12f
    inline val widthInUnits: Float get() = heightInTiles / height * width

    var width: Int
        set(value) {
            _width = value
            GLFW.glfwSetWindowSize(id, width, height)
        }
        get() = _width

    var height: Int
        set(value) {
            _height = value
            GLFW.glfwSetWindowSize(id, width, height)
        }
        get() = _height

    /**
     * The aspect ratio of the display (width:height)
     */
    inline val aspectRatio: Float
        get() = width.toFloat() / height

    /**
     * Sets window title
     */
    var title: String = "game"
        set(value) {
            field = value
            GLFW.glfwSetWindowTitle(id, value)
        }

    /**
     * This is pretty self-explanatory, isn't it?
     */
    inline val shouldClose get() = GLFW.glfwWindowShouldClose(id)

    /**
     * Sets the window to be fullscreen or not
     */
    var isFullscreen = false
        set(value) {
            field = value
            if (value) {
                GLFW.glfwMaximizeWindow(id)
            } else {
                GLFW.glfwRestoreWindow(id)
            }
        }

    var id: Long = 0
        private set

    companion object {
        fun init() {
            check(GLFW.glfwInit()) {
                "[GLFW ERROR]: GLFW wasn't inititalized"
            }
            GLFWErrorCallback.createPrint().set()
        }
    }

    private var _width: Int = 0
    private var _height: Int = 0

    internal fun init(
        log: MainLogger,
        width: Int,
        height: Int,
        title: String?,
        callback: InputManager
    ) {
        _width = width
        _height = height
        id = GLFW.glfwCreateWindow(_width, _height, this.title, 0, 0)
        if (title != null) {
            this.title = title
        }
        if (id == 0L) {
            log.e("[GLFW ERROR]: Window wasn't created")
            return
        }
        val videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
        GLFW.glfwSetWindowPos(id, (videoMode!!.width() - this.width) / 2, (videoMode.height() - this.height) / 2)
        GLFW.glfwSetWindowSizeLimits(id, 600, 300, -1, -1)
        initCallbacks(callback)
        GLFW.glfwShowWindow(id)
        log.verbose {
            i("Created window: ")
            i(this@Window)
        }
    }

    internal inline fun pollEvents() = GLFW.glfwPollEvents()
    internal inline fun swapBuffers() = GLFW.glfwSwapBuffers(id)

    internal fun destroy() {
        destroyCallbacks()
        GLFW.glfwDestroyWindow(id)
        GLFW.glfwTerminate()
    }

    override fun describe(): String =
        """Window { 
        |    id: ${Format.pointer(id)}
        |    width: $width
        |    height: $height
        |    title: "${Format.doubleQuotesEscape(title)}"
        |}""".trimMargin()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Window

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()
    
    private fun initCallbacks(input: InputManager) {
        input.init(this)
        GLFW.glfwSetKeyCallback(id) { _, key, scanCode, action, mods ->
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                input.onKeyPressed(this, key, scanCode, action, mods)
            }
        }
        GLFW.glfwSetMouseButtonCallback(id) { _, btn, action, mods ->
            input.onMouseButtonPress(this, btn, action, mods)
        }
        GLFW.glfwSetScrollCallback(id) { _, x, y ->
            input.onScroll(this, x, y)
        }
        GLFW.glfwSetCursorPosCallback(id) { _, x, y -> input.onMouseMove(this, x, y) }
        GLFW.glfwSetWindowSizeCallback(id) { _: Long, w: Int, h: Int ->
            _width = w
            _height = h
            renderer.onWindowResize(w, h)
        }
    }
    
    private fun destroyCallbacks() {
        GLFW.glfwSetKeyCallback(id, null)?.free()
        GLFW.glfwSetCursorPosCallback(id, null)?.free()
        GLFW.glfwSetMouseButtonCallback(id, null)?.free()
        GLFW.glfwSetScrollCallback(id, null)?.free()
        GLFW.glfwSetWindowSizeCallback(id, null)?.free()
    }
}