package io.posidon.uranium.input

import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.mathlib.types.functions.*
import io.posidon.uranium.util.Heap
import io.posidon.uranium.util.set
import io.posidon.uranium.window.Window
import org.lwjgl.glfw.GLFW
import java.nio.IntBuffer
import java.util.*

typealias OnKeyPressedListener = (window: Window, key: Int, scanCode: Int, action: Int, mods: Int) -> Unit
typealias OnScrollListener = (window: Window, x: Double, y: Double) -> Unit
typealias OnClickListener = (window: Window, button: Int, action: Int, mods: Int, cursorX: Double, cursorY: Double) -> Unit
typealias OnCursorMoveListener = (window: Window, x: Double, y: Double, distanceX: Double, distanceY: Double) -> Unit

class InputManager {

    fun isKeyPressed(key: Int) = keys[key] != Action.RELEASE
    fun isButtonPressed(button: Int) = mouseButtons[button] != Action.RELEASE

    /**
     * Whether the cursor is in it's normal state, or locked to the window and invisible
     */
    var mouseLocked = false
        set(value) {
            if (value) GLFW.glfwSetCursorPos(windowID, curX, curY)
            field = value
            GLFW.glfwSetInputMode(windowID, GLFW.GLFW_CURSOR, if (value) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL)
        }

    fun addOnKeyPressedListener(listener: OnKeyPressedListener) { onKeyPressedListeners += listener }
    fun removeOnKeyPressedListener(listener: OnKeyPressedListener) { onKeyPressedListeners -= listener }

    fun addOnScrollListener(listener: OnScrollListener) { onScrollListeners += listener }
    fun removeOnScrollListener(listener: OnScrollListener) { onScrollListeners -= listener }

    fun addOnClickListener(listener: OnClickListener) { onClickListeners += listener }
    fun removeOnClickListener(listener: OnClickListener) { onClickListeners -= listener }

    fun addOnCursorMoveListener(listener: OnCursorMoveListener) { onCursorMoveListeners += listener }
    fun removeOnCursorMoveListener(listener: OnCursorMoveListener) { onCursorMoveListeners -= listener }

    var goUp = false
        private set
    var goLeft = false
        private set
    var goDown = false
        private set
    var goRight = false
        private set

    fun getWalkDirection(): Vec2f {
        var x = 0f
        if (goLeft) x--
        if (goRight) x++
        var y = 0f
        if (goDown) y--
        if (goUp) y++
        return Vec2f(x, y).apply { selfNormalize() }
    }

    internal var curX = 0.0
    internal var curY = 0.0

    internal fun onKeyPressed(window: Window, key: Int, scanCode: Int, action: Int, mods: Int) {
        keys[key] = action
        when (key) {
            Key.W -> goUp = action != Action.RELEASE
            Key.A -> goLeft = action != Action.RELEASE
            Key.S -> goDown = action != Action.RELEASE
            Key.D -> goRight = action != Action.RELEASE
        }
        onKeyPressedListeners.forEach {
            it(window, key, scanCode, action, mods)
        }
    }

    internal fun onMouseButtonPress(window: Window, btn: Int, action: Int, mods: Int) {
        mouseButtons[btn] = action
        onClickListeners.forEach {
            it(window, btn, action, mods, curX, curY)
        }
    }

    internal fun onScroll(window: Window, x: Double, y: Double) {
        onScrollListeners.forEach {
            it(window, x, y)
        }
    }

    private var oldCurX = 0.0
    private var oldCurY = 0.0
    internal fun onMouseMove(window: Window, x: Double, y: Double) {
        curX = x
        curY = y

        val dx = curX - oldCurX
        val dy = curY - oldCurY
        onCursorMoveListeners.forEach {
            it(window, x, y, dx, dy)
        }

        oldCurX = curX
        oldCurY = curY
    }

    internal fun init(window: Window) {
        windowID = window.id
        keys = Heap.callocInt(GLFW.GLFW_KEY_LAST)
        mouseButtons = Heap.callocInt(GLFW.GLFW_MOUSE_BUTTON_LAST)
    }

    internal fun destroy() {
        Heap.free(keys)
        Heap.free(mouseButtons)
    }

    private var windowID = 0L
    private lateinit var keys: IntBuffer
    private lateinit var mouseButtons: IntBuffer

    private val onKeyPressedListeners = LinkedList<OnKeyPressedListener>()
    private val onScrollListeners = LinkedList<OnScrollListener>()
    private val onClickListeners = LinkedList<OnClickListener>()
    private val onCursorMoveListeners = LinkedList<OnCursorMoveListener>()
}