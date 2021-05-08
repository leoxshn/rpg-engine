package io.posidon.rpg.world.tile

import io.posidon.game.shared.types.Vec2i

class TileMeta(
    val width: Int,
    val height: Int,
    val bitmasks: Map<Int, Vec2i>
)