package io.posidon.rpgengine.gfx

class FrameSynchronizer {

    private var lastRenderTime = 0L

    fun init() {
        lastRenderTime = System.nanoTime()
    }

    fun sync(fps: Int) {
        val now = System.nanoTime()
        val renderNanos = (now - lastRenderTime)
        lastRenderTime = now
        val sleep = 1000f / fps - renderNanos / 1000_000f
        if (sleep > 0f) {
            Thread.sleep(sleep.toLong())
        }
    }
}