package io.posidon.rpgengine.scene.node.container

import io.posidon.game.shared.types.Vec2f
import io.posidon.game.shared.types.Vec2i
import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.input.InputManager
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.Positional
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window

open class ChunkMap <K> (val isLoaded: (K) -> Boolean) : Node() {

    private var initialized = false

    val chunks = HashMap<K, Chunk<K>>()

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
            nodes.add(node)
            if (map.initialized) {
                node.internalInit(map.log, map.context, map.input)
            }
        }

        fun remove(node: Node) {
            nodes.remove(node)
        }

        private val nodes = ArrayList<Node>()

        fun render(renderer: Renderer, window: Window) {
            nodes.forEach {
                it.render(renderer, window)
            }
        }

        fun update(delta: Float) {
            nodes.forEach {
                it.update(delta)
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
    }
}

class ChunkMap2D (val chunkSize: Int, isLoaded: (Vec2i) -> Boolean) : ChunkMap<Vec2i>(isLoaded) {

    fun add(node: Node, position: Vec2f) = add(node, position.toVec2i())

    fun add(node: Node, position: Vec2i) {
        val chunk = chunks.getOrPut(position / chunkSize) {
            Chunk(this)
        }
        chunk.add(node)
    }
}

@JvmName("plusAssignFloat")
operator fun ChunkMap2D.plusAssign(node: Positional<Vec2f>) = add(node as Node, node.position)
@JvmName("plusAssignInt")
operator fun ChunkMap2D.plusAssign(node: Positional<Vec2i>) = add(node as Node, node.position)