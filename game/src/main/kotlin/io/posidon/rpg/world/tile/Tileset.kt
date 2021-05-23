package io.posidon.rpg.world.tile

import io.posidon.game.shared.Resources
import io.posidon.rpgengine.debug.MainLogger
import io.posidon.rpgengine.gfx.Context
import io.posidon.rpgengine.gfx.assets.Texture

class Tileset(
    val texture: Texture,
    val meta: TileMeta
) {
    fun destroy() {
        texture.destroy()
    }
}

fun Context.loadTileset(log: MainLogger, path: String): Tileset {
    val texture = loadTexture(log, "$path.png")
    val metaCode = Resources.loadAsString("$path.tilemeta")
    val meta = TileMeta.parse(metaCode)
    return Tileset(texture, meta)
}