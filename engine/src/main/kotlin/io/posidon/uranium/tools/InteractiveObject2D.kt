package io.posidon.uranium.tools

import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.scene.Positional
import io.posidon.uranium.scene.node.Node

abstract class InteractiveObject2D(
    override val position: Vec2f
) : Node(), Positional<Vec2f> {

    override fun init() {
    }

    override fun destroy() {
        super.destroy()
    }
}