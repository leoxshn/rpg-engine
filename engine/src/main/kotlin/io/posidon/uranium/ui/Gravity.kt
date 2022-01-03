package io.posidon.uranium.ui

object Gravity {
    const val TOP = 0b0001
    const val BOTTOM = 0b0010
    const val CENTER_VERTICAL = TOP or BOTTOM

    const val LEFT = 0b0100
    const val RIGHT = 0b1000
    const val CENTER_HORIZONTAL = LEFT or RIGHT

    const val CENTER = CENTER_VERTICAL or CENTER_HORIZONTAL
}