package io.posidon.uranium.util

import io.posidon.uranium.mathlib.types.Vec4f

fun Vec4f.Companion.fromColorInt(color: Int): Vec4f {
    val a = (color shr (8 * 3) and 0xff) / 256f
    val r = (color shr (8 * 2) and 0xff) / 256f
    val g = (color shr (8 * 1) and 0xff) / 256f
    val b = (color             and 0xff) / 256f
    return Vec4f(r, g, b, a)
}