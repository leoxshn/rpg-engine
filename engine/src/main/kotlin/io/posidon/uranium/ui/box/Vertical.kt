package io.posidon.uranium.ui.box

import io.posidon.uranium.ui.UIComponent

class Vertical : UILayout {

    private var h = -1f

    override fun calculateWidth(children: List<UIComponent>): Float =
        children.maxOf { it.getWidth() }

    override fun calculateHeight(children: List<UIComponent>): Float {
        if (h == -1f) {
            update(children)
        }
        return h
    }

    override fun update(children: List<UIComponent>) {
        var lastPos = 0f
        var lastSize = 0f
        for (c in children) {
            c.position.y = lastPos + lastSize
            lastPos = c.position.y
            lastSize = c.getHeight()
        }
        h = lastPos + lastSize
    }
}