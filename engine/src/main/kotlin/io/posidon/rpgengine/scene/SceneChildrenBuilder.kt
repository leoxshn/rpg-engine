package io.posidon.rpgengine.scene

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.gfx.assets.Uniforms
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.gfx.renderer.UIRenderer
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.tools.Filter
import io.posidon.rpgengine.tools.Camera2D
import io.posidon.rpgengine.window.Window
import java.util.LinkedList

class SceneChildrenBuilder(
    val renderer: Renderer,
    private val window: Window
) {

    internal val layers = LinkedList<LayerBuilder>()

    fun customLayer(renderer: Renderer, init: LayerBuilder.() -> Unit) {
        layers += LayerBuilder(renderer, window).apply(init)
    }

    fun camera2DLayer(cameraFollowPos: Vec2f, init: LayerBuilderWithCamera2D.() -> Unit) {
        layers += LayerBuilderWithCamera2D(Camera2D(renderer, cameraFollowPos), window).apply(init)
    }

    fun uiLayer(init: LayerBuilder.() -> Unit) = customLayer(UIRenderer(renderer), init)

    class LayerBuilderWithCamera2D internal constructor(
        val camera: Camera2D,
        window: Window
    ) : LayerBuilder(camera.renderer, window)

    class FilterBuilder internal constructor(
    ) {
        internal val nodes = LinkedList<Node>()

        operator fun Node.unaryMinus() {
            nodes += this
        }

        internal var uniforms: Uniforms.() -> Unit = {}
            private set

        fun shader(block: Uniforms.() -> Unit) {
            uniforms = block
        }
    }

    open class LayerBuilder internal constructor(
        internal val renderer: Renderer,
        private val window: Window
    ) {

        internal val nodes = LinkedList<Node>()

        operator fun Node.unaryMinus() {
            nodes += this
        }

        fun postprocessing(fragmentPath: String, colorBufferCount: Int, block: FilterBuilder.() -> Unit) {
            val builder = FilterBuilder().apply(block)
            nodes += Filter(renderer, window, fragmentPath, colorBufferCount, builder.uniforms, builder.nodes)
        }
    }
}
