package io.posidon.game.netApi

import io.posidon.game.shared.types.Vec2f

inline class Packet internal constructor(val string: String) {

    inline val tokens get() = string.split(SEPARATOR)
    inline val type get() = string.substringBefore(SEPARATOR)

    inline fun parseTime(): Double = tokens[1].toDouble()

    inline fun parsePosition(): Vec2f = Vec2f(tokens[1].toFloat(), tokens[2].toFloat())

    inline fun parseAuth(): String = string

    companion object {
        const val SEPARATOR = '&'

        internal inline fun make(packetType: String, vararg data: Any): Packet {
            return Packet(buildString {
                append(packetType)
                for (s in data) {
                    append(SEPARATOR)
                    append(s)
                }
            })
        }
    }
}