package io.posidon.uranium.gfx.assets

import io.posidon.uranium.mathlib.types.*

interface Shader {
    operator fun set(name: String, value: Float)
    operator fun set(name: String, value: Int)
    operator fun set(name: String, value: Boolean)
    operator fun set(name: String, value: Vec2f)
    operator fun set(name: String, value: Vec2i)
    operator fun set(name: String, value: Vec3f)
    operator fun set(name: String, value: Vec3i)
    operator fun set(name: String, value: Vec4f)
    operator fun set(name: String, value: Mat4f)
    fun bind()

    fun destroy()
}

@JvmInline
value class Uniforms(val shader: Shader) {

    inline infix fun String.set(value: Float) = shader.set(this, value)
    inline infix fun String.set(value: Int) = shader.set(this, value)
    inline infix fun String.set(value: Boolean) = shader.set(this, value)
    inline infix fun String.set(value: Vec2f) = shader.set(this, value)
    inline infix fun String.set(value: Vec2i) = shader.set(this, value)
    inline infix fun String.set(value: Vec3f) = shader.set(this, value)
    inline infix fun String.set(value: Vec3i) = shader.set(this, value)
    inline infix fun String.set(value: Vec4f) = shader.set(this, value)
    inline infix fun String.set(value: Mat4f) = shader.set(this, value)

    inline infix fun String.set(value: IntArray) {
        for (i in value.indices) "$this[$i]" set value[i]
    }
    inline infix fun String.set(value: FloatArray) {
        for (i in value.indices) "$this[$i]" set value[i]
    }
    inline infix fun String.set(value: BooleanArray) {
        for (i in value.indices) "$this[$i]" set value[i]
    }
    inline infix fun String.set(value: Array<Vec2f>) {
        for (i in value.indices) "$this[$i]" set value[i]
    }
    inline infix fun String.set(value: Array<Vec3f>) {
        for (i in value.indices) "$this[$i]" set value[i]
    }
}

inline operator fun Shader.invoke(block: Uniforms.() -> Unit) {
    bind()
    block(Uniforms(this))
}