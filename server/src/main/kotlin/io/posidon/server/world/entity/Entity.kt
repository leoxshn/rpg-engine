package io.posidon.server.world.entity

import io.posidon.game.shared.types.Vec2i

abstract class Entity {
    abstract val position: Vec2i

    var id: Long = 0
        private set

    fun init() {
        id = EntityManager.create(this)
    }

    fun destroy() {
        EntityManager.remove(id)
    }

    fun packAsString(): String {
        return "entity&${position.x}&${position.y}"
    }
}