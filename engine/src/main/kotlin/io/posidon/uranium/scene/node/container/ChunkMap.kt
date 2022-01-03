package io.posidon.uranium.scene.node.container

import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.mathlib.types.functions.*
import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.input.InputManager
import io.posidon.uranium.gfx.Context
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.mathlib.types.Vec2i
import io.posidon.uranium.scene.Positional
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.window.Window
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.ceil
import kotlin.math.floor

open class ChunkMap <K> (isLoaded: (K) -> Boolean) : Node() {

    var isLoaded = isLoaded
        private set

    fun setIsLoaded(isLoaded: (K) -> Boolean) {
        this.isLoaded = isLoaded
    }

    private var initialized = false

    val chunks = ConcurrentHashMap<K, Chunk<K>>()

    override fun render(renderer: Renderer, window: Window) {
        for (e in chunks) {
            if (isLoaded(e.key)) e.value.render(renderer, window)
        }
    }

    override fun update(delta: Float) {
        for (e in chunks) {
            if (isLoaded(e.key)) e.value.update(delta)
        }
    }

    override fun destroy() {
        for (e in chunks) {
            e.value.destroy()
        }
    }

    override fun internalInit(log: MainLogger, context: Context, input: InputManager) {
        super.internalInit(log, context, input)
        initialized = true
        for (e in chunks) {
            e.value.internalInit(log, context, input)
        }
    }

    class Chunk <T> (val map: ChunkMap<T>) {

        fun add(node: Node) {
            map.onRenderThread {
                synchronized(nodes) {
                    nodes += node
                }
                if (map.initialized) {
                    node.internalInit(map.log, map.context, map.input)
                }
            }
        }

        fun remove(node: Node) {
            map.onRenderThread {
                synchronized(nodes) {
                    nodes -= node
                }
            }
        }

        private val nodes = ConcurrentLinkedQueue<Node>()

        fun render(renderer: Renderer, window: Window) {
            nodes.forEach {
                it.render(renderer, window)
            }
        }

        fun update(delta: Float) {
            synchronized(nodes) {
                nodes.forEach {
                    it.update(delta)
                }
            }
        }

        fun destroy() {
            nodes.forEach(Node::destroy)
        }

        internal fun internalInit(log: MainLogger, context: Context, input: InputManager) {
            nodes.forEach {
                it.internalInit(log, context, input)
            }
        }

        operator fun iterator() = nodes.iterator()
    }
}

class ChunkMap2D (val chunkSize: Int, isLoaded: (Vec2i) -> Boolean = { true }) : ChunkMap<Vec2i>(isLoaded) {

    fun add(node: Node, position: Vec2f) = add(node, position.toVec2i())

    fun add(node: Node, position: Vec2i) {
        val chunk = chunks.getOrPut(position / chunkSize) {
            Chunk(this)
        }
        chunk.add(node)
    }

    fun remove(node: Node) {
        chunks.forEach { (_, c) -> c.remove(node) }
    }

    /**
     * Lists and sorts all nodes in the [radius] around the [position] by distance from the [position]
     * Only positional nodes are included
     */
    inline fun <reified T> getWithinRadius(position: Vec2f, radius: Float): List<T> {
        val buf = LinkedList<Pair<Float, T>>()
        val xr = floor(position.x - radius).toInt() / chunkSize - 1..ceil(position.x + radius).toInt() / chunkSize
        val yr = floor(position.y - radius).toInt() / chunkSize - 1..ceil(position.y + radius).toInt() / chunkSize
        for (cx in xr) for (cy in yr) {
            val chunk = chunks[Vec2i(cx, cy)] ?: continue
            for (node in chunk) {
                if (node is Positional<*> && node is T) {
                    when (val np = node.position) {
                        is Vec2i -> {
                            val distance = (np.toVec2f() - position).length
                            if (distance <= radius) {
                                buf += distance to node
                            }
                        }
                        is Vec2f -> {
                            val distance = (np - position).length
                            if (distance <= radius) {
                                buf += distance to node
                            }
                        }
                    }
                }
            }
        }
        return buf.apply { sortBy { it.first } }.map { it.second }
    }
}

@JvmName("plusAssignFloat")
inline operator fun ChunkMap2D.plusAssign(node: Positional<Vec2f>) = add(node as Node, node.position)
@JvmName("plusAssignInt")
inline operator fun ChunkMap2D.plusAssign(node: Positional<Vec2i>) = add(node as Node, node.position)

inline operator fun ChunkMap2D.minusAssign(node: Node) = remove(node)

inline operator fun ChunkMap.Chunk<*>.plusAssign(node: Node) = add(node)