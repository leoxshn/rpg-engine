package io.posidon.uranium.scene.node.container

import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.input.InputManager
import io.posidon.uranium.gfx.Context
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.window.Window
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
            node.init()
        }
    }

    private var initialized = false

    override fun init() {
        for (n in nodes) {
            n.init()
        }
    }

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