package io.posidon.uranium.ui.box

import io.posidon.uranium.ui.UIComponent

interface UILayout {
    fun calculateWidth(children: List<UIComponent>): Float
    fun calculateHeight(children: List<UIComponent>): Float
    fun update(children: List<UIComponent>)
}