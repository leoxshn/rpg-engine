package io.posidon.server.world.entity

import kotlin.random.Random

object EntityManager {
    private val entities = HashMap<Long, Entity>()

    operator fun get(id: Long): Entity? = entities[id]

    tailrec fun create(entity: Entity): Long {
        val id = Random.nextLong()
        if (entities.containsKey(id)) {
            return create(entity)
        }
        entities[id] = entity
        return id
    }

    fun remove(id: Long) {
        entities.remove(id)
    }
}