package io.posidon.game.netApi

import io.posidon.game.shared.types.Position
import io.posidon.game.shared.types.Vec3f
import io.posidon.game.shared.types.Vec3i

inline class Packet internal constructor(val string: String) {

    inline val tokens get() = string.split(SEPARATOR)
    inline val type get() = string.substringBefore(SEPARATOR)

    inline fun parseTime(): Double = tokens[1].toDouble()

    inline fun parsePosition(): Position = Position(tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toInt())
    inline fun parseFloatCoords(): Vec3f = Vec3f(tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())
    inline fun parseIntCoords(): Vec3i = Vec3i(tokens[1].toInt(), tokens[2].toInt(), tokens[3].toInt())

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