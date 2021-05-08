package io.posidon.game.shared.types

import java.util.*
import kotlin.math.*

data class Position(val xy: Vec2f, var h: Int) {

    var x by xy::x
    var y by xy::y

    inline fun set(v: Vec3i) = set(v.x.toFloat(), v.y.toFloat(), v.z)
    inline fun set(v: Position) = set(v.x, v.y, v.h)
    inline fun set(x: Float, y: Float, h: Int) {
        xy.set(x, y)
        this.h = h
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false
        return other.x.compareTo(x) == 0 && other.y.compareTo(y) == 0 && other.h.compareTo(h) == 0
    }

    override fun hashCode() = Objects.hash(x, y, h)
    override fun toString() = "position($x, $y, $h)"

    companion object {
        inline fun zero() = Position(Vec2f.zero(), 0)
    }
}

fun Position(x: Float, y: Float, h: Int): Position = Position(Vec2f(x, y), h)