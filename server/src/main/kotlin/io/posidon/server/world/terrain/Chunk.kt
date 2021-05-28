package io.posidon.server.world.terrain

import io.posidon.server.world.entity.Entity

fun Chunk(
    initTemperature: (Int) -> Float,
    initViscosity: (Int) -> Float
): Chunk {
    return Chunk(
        FloatArray(Chunk.SIZE * Chunk.SIZE, initTemperature),
        FloatArray(Chunk.SIZE * Chunk.SIZE, initViscosity),
        arrayListOf())
}

class Chunk(
    val temperature: FloatArray,
    val viscosity: FloatArray,
    val entities: ArrayList<Entity>
) {

    fun set(x: Int, y: Int, probe: Probe) {
        val i = x * SIZE + y
        this.temperature[i] = probe.temperature
        this.viscosity[i] = probe.viscosity
    }

    fun probe(x: Int, y: Int): Probe {
        val i = x * SIZE + y
        return Probe(temperature[i], viscosity[i])
    }

    fun makePacketString(): String {
        return entities.joinToString("&", transform = Entity::packAsString)
    }

    companion object {
        const val SIZE = 32
    }
}