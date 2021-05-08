package io.posidon.game.shared

inline fun invSqrt(x: Float): Float {
    var x = x
    val xhalf = 0.5f * x
    var i = x.toRawBits()
    i = 0x5f3759df - (i shr 1)
    x = java.lang.Float.intBitsToFloat(i)
    x *= 1.5f - xhalf * x * x
    return x
}

inline fun fastSqrt(x: Float): Float = invSqrt(x) * x