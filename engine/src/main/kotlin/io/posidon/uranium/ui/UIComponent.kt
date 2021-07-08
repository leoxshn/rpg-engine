package io.posidon.uranium.ui

import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.scene.Positional
import io.posidon.uranium.window.Window

interface UIComponent : Positional<Vec2f> {
    fun render(renderer: Renderer, window: Window)

    fun getWidth(): Float
    fun getHeight(): Float
}