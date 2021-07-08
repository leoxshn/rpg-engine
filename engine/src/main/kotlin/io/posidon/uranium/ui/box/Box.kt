package io.posidon.uranium.ui.box

import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.scene.node.container.NodeGroup
import io.posidon.uranium.ui.UIComponent
import java.util.*

class Box(
    override val position: Vec2f,
    val UILayout: UILayout,
    nodes: LinkedList<Node>,
) : NodeGroup<Node>(nodes), UIComponent {

    private val children = nodes.filter { it is UIComponent } as MutableList<UIComponent>

    override fun onAdd(node: Node) {
        if (node is UIComponent) children.add(node)
    }

    override fun update(delta: Float) {
        UILayout.update(children)
    }

    override fun getWidth(): Float = UILayout.calculateWidth(children)
    override fun getHeight(): Float = UILayout.calculateHeight(children)
}