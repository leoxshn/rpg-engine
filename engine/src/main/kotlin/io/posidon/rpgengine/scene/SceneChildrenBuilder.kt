package io.posidon.rpgengine.scene

import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.node.Node
import java.util.*

class SceneChildrenBuilder(
    var renderer: Renderer
) {
    operator fun Node.unaryMinus() {
        nodes.add(this)
    }
    internal val nodes = LinkedList<Node>()
}
