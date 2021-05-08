package io.posidon.rpgengine.gfx.assets

import io.posidon.game.shared.types.Mat4f
import io.posidon.game.shared.types.Vec2f
import io.posidon.game.shared.types.Vec3f
import io.posidon.game.shared.types.Vec3i

interface Shader {
    operator fun set(name: String, value: Float)
    operator fun set(name: String, value: Int)
    operator fun set(name: String, value: Boolean)
    operator fun set(name: String, value: Vec2f)
    operator fun set(name: String, value: Vec3f)
    operator fun set(name: String, value: Vec3i)
    operator fun set(name: String, value: Mat4f)
    fun bind()

    fun destroy()
}

operator fun Shader.set(name: String, value: IntArray) {
    for (i in value.indices) set("$name[$i]", value[i])
}
operator fun Shader.set(name: String, value: FloatArray) {
    for (i in value.indices) set("$name[$i]", value[i])
}
operator fun Shader.set(name: String, value: BooleanArray) {
    for (i in value.indices) set("$name[$i]", value[i])
}
operator fun Shader.set(name: String, value: Array<Vec2f>) {
    for (i in value.indices) set("$name[$i]", value[i])
}
operator fun Shader.set(name: String, value: Array<Vec3f>) {
    for (i in value.indices) set("$name[$i]", value[i])
}