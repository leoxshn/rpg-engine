package io.posidon.rpgengine.util

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

operator fun ByteBuffer.set(i: Int, v: Byte): ByteBuffer = put(i, v)
operator fun IntBuffer.set(i: Int, v: Int): IntBuffer = put(i, v)
operator fun FloatBuffer.set(i: Int, v: Float): FloatBuffer = put(i, v)
operator fun ShortBuffer.set(i: Int, v: Short): ShortBuffer = put(i, v)