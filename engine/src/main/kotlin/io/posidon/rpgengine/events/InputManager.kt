package io.posidon.rpgengine.events

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.input.Action
import io.posidon.rpgengine.input.Key
import io.posidon.rpgengine.util.set
import io.posidon.rpgengine.window.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryUtil
import java.nio.IntBuffer

class InputManager {

    /**
     * Whether the cursor is in it's normal state, or locked to the window and invisible
     */
    var mouseLocked = false
        set(value) {
            if (value) GLFW.glfwSetCursorPos(windowID, curX, curY)
            field = value
            GLFW.glfwSetInputMode(windowID, GLFW.GLFW_CURSOR, if (value) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL)
        }

    private var windowID = 0L
    private lateinit var keys: IntBuffer
    private lateinit var mouseButtons: IntBuffer

    internal var curX = 0.0
    internal var curY = 0.0

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

    fun onKeyPressed(window: Window, key: Int, scanCode: Int, action: Int, mods: Int) {
        keys[key] = action
        when (key) {
            Key.W -> goUp = action != Action.RELEASE
            Key.A -> goLeft = action != Action.RELEASE
            Key.S -> goDown = action != Action.RELEASE
            Key.D -> goRight = action != Action.RELEASE
        }
    }

    fun onMouseButtonPress(window: Window, btn: Int, action: Int, mods: Int) {
        mouseButtons[btn] = action
    }

    fun onScroll(window: Window, x: Double, y: Double) {

    }

    private var oldCurX = 0.0
    private var oldCurY = 0.0
    fun onMouseMove(window: Window, x: Double, y: Double) {
        if (mouseLocked) {
            curX = x
            curY = y

            val dx = (curX - oldCurX).toFloat()
            val dy = (curY - oldCurY).toFloat()

            oldCurX = curX
            oldCurY = curY
        }
    }

    fun init(window: Window) {
        windowID = window.id
        keys = MemoryUtil.memCallocInt(GLFW.GLFW_KEY_LAST)
        mouseButtons = MemoryUtil.memCallocInt(GLFW.GLFW_MOUSE_BUTTON_LAST)
    }

    fun destroy() {
        MemoryUtil.memFree(keys)
        MemoryUtil.memFree(mouseButtons)
    }
}