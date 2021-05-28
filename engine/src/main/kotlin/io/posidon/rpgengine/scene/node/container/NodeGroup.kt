package io.posidon.rpgengine.scene.node.container

import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.input.InputManager
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.window.Window
import java.util.*

open class NodeGroup : Node {

    constructor() : this(LinkedList())
    constructor(nodes: MutableList<Node>) : super() {
        this.nodes = nodes
    }

    private val nodes: MutableList<Node>

    fun add(node: Node) {
        nodes += node
        if (initialized) {
            node.internalInit(log, context, input)
        }
    }

    private var initialized = false

    override fun render(renderer: Renderer, window: Window) {
        for (n in nodes) {
            n.render(renderer, window)
        }
    }

    override fun update(delta: Float) {
        for (n in nodes) {
            n.update(delta)
        }
    }

    override fun destroy() {
        for (n in nodes) {
            n.destroy()
        }
    }

    override fun internalInit(log: MainLogger, context: Context, input: InputManager) {
        super.internalInit(log, context, input)
        initialized = true
        for (n in nodes) {
            n.internalInit(log, context, input)
        }
    }
}

inline operator fun NodeGroup.plusAssign(node: Node) = add(node)