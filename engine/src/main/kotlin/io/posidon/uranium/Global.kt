package io.posidon.uranium

object Global {
    fun millis(): Long = System.currentTimeMillis() - im

    private val im = System.currentTimeMillis()
}