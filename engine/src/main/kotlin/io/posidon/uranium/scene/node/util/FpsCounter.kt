package io.posidon.uranium.scene.node.util

import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.gfx.assets.Font
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.scene.node.container.NodeWrapper
import io.posidon.uranium.ui.text.Text
import io.posidon.uranium.window.Window
import kotlin.math.roundToInt

class FpsCounter(
    fontHeight: Float,
    position: Vec2f,
    font: Font
) : NodeWrapper<Text>(Text(fontHeight, "fps: __", position, font)) {

    inline val font get() = node?.font
    inline val fontHeight get() = node?.fontHeight
    inline val position get() = node?.position

    private var ups = 0
    private var lastUpdateTime by onInitMutable { System.nanoTime() }

    override fun update(delta: Float) {
        super.update(delta)
        val now = System.nanoTime()
        val s = (now - lastUpdateTime) / 1000_000_000.0
        ups = (1 / s).roundToInt()
        lastUpdateTime = now
    }

    private var fps = 0
    private var lastRenderTime by onInitMutable { System.nanoTime() }

    override fun render(renderer: Renderer, window: Window) {
        super.render(renderer, window)
        val now = System.nanoTime()
        val s = (now - lastRenderTime) / 1000_000_000.0
        fps = (1 / s).roundToInt()
        lastRenderTime = now
        node?.text = "fps: $fps\nups: $ups"
    }
}