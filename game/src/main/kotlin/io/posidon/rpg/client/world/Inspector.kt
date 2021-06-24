package io.posidon.rpg.client.world

import io.posidon.rpg.client.world.entities.EntityNode
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.gfx.assets.Font
import io.posidon.uranium.scene.node.container.NodeWrapper
import io.posidon.uranium.ui.text.Text
import io.posidon.uranium.ui.text.c

class Inspector(
    position: Vec2f,
    font: Font
) : NodeWrapper<Text>(Text(18f, "", position, font)) {

    val redMatterSymbol by onInit { node!!.customSymbol(context, log, "/textures/symbol/red_matter.png") }
    val greenMatterSymbol by onInit { node!!.customSymbol(context, log, "/textures/symbol/green_matter.png") }
    val blueMatterSymbol by onInit { node!!.customSymbol(context, log, "/textures/symbol/blue_matter.png") }
    val stringMatterSymbol by onInit { node!!.customSymbol(context, log, "/textures/symbol/string_matter.png") }

    fun setEntity(entity: EntityNode?) {
        text = if (entity == null) "" else """
           |composition:
           |  ${c(0xffff8a22, "$redMatterSymbol: ${entity.composition.redMatter}%")}
           |  ${c(0xff8aff33, "$greenMatterSymbol: ${entity.composition.greenMatter}%")}
           |  ${c(0xffba88ff, "$blueMatterSymbol: ${entity.composition.blueMatter}%")}
           |  ${c(0xff22edff, "$stringMatterSymbol: ${entity.composition.stringMatter}%")}
        """.trimMargin()
    }

    private var text by node!!::text
}
