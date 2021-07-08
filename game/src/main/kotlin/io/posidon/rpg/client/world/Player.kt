package io.posidon.rpg.client.world

import io.posidon.rpg.client.world.entities.EntityNode
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.Global
import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.gfx.renderer.renderMesh2D
import io.posidon.uranium.input.Key
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.scene.node.container.ChunkMap2D
import io.posidon.uranium.window.Window
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

class Player(
    val chunkMap: ChunkMap2D,
    val inspector: Inspector
) : Node() {

    val position = Vec2f.zero()
    val moveSpeed = 7.5f
    val acceleration = 12f

    val body by mesh(0, 1, 2) {
        2.v(-.5f, -.5f, .5f, -.5f, 0f, .6f)
    }

    val shader by objectShader("/shaders/objects/player.fsh")

    val spriteWidth = 1f
    val spriteHeight = 1.6f

    private var rotation = 0f
    private var targetRotation = 0f
    private var choppedRotation = 0f

    override fun render(renderer: Renderer, window: Window) {
        shader {
            "millis" set Global.millis().toFloat()
        }
        renderer.renderMesh2D(body, window, shader, position, spriteWidth, spriteHeight, choppedRotation)
    }

    private var chosenEntity: EntityNode? = null
    var closestEntities = emptyList<EntityNode>()
        private set

    val velocity = Vec2f.zero()
    private var scr = 0f
    private val animationFrameSeconds = 1f / 24f

    override fun update(delta: Float) {
        val dir = input.getWalkDirection()
        velocity.selfMix(dir * moveSpeed * delta, acceleration * delta)
        position.selfAdd(velocity)
        if (!dir.isZero) {
            val a = atan2(dir.x, dir.y)
            val b = a + 2.0 * PI
            val c = a - 2.0 * PI
            val ab = if (abs(a - choppedRotation) < abs(b - choppedRotation)) a else b.toFloat()
            targetRotation = if (abs(ab - choppedRotation) < abs(c - choppedRotation)) ab else c.toFloat()
            val smoothing = (1f - delta * 10f)
            rotation = rotation * smoothing + targetRotation * (1f - smoothing)
            rotation %= (2.0 * PI).toFloat()
        }
        if (!velocity.isZero) {
            scr += delta
            if (scr >= animationFrameSeconds) {
                scr -= animationFrameSeconds
                choppedRotation = rotation
            }
            updateEntities()
        } else {
            scr = 0f
        }
        if (input.isKeyPressed(Key.R)) {
            chosenEntity?.tryBreak(delta, chunkMap)
        }
    }

    internal var selectionI = 0
        private set
    private fun updateEntities() {
        val newClosestEntities = chunkMap.getWithinRadius<EntityNode>(position, 7f)
        if (newClosestEntities != closestEntities) {
            closestEntities = newClosestEntities
            val newI = newClosestEntities.indexOf(chosenEntity)
            if (newI == -1) {
                selectionI = 0
                chosenEntity = closestEntities.firstOrNull()
                inspector.setEntity(chosenEntity)
            } else {
                selectionI = newI
            }
        } else {
            val new = closestEntities.getOrNull(selectionI)
            if (new != null) {
                if (new != chosenEntity) {
                    chosenEntity = new
                    inspector.setEntity(chosenEntity)
                }
            } else {
                selectionI = 0
            }
        }
    }

    private fun onScroll(window: Window, x: Double, y: Double) {
        if (closestEntities.isNotEmpty()) {
            selectionI = ++selectionI % closestEntities.size
        }
    }

    override fun init() {
        input.addOnScrollListener(::onScroll)
    }

    override fun destroy() {
        shader.destroy()
        input.removeOnScrollListener(::onScroll)
    }
}
