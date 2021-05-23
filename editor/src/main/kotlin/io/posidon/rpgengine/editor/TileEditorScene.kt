package io.posidon.rpgengine.editor

import io.posidon.rpgengine.scene.Scene
import io.posidon.rpgengine.scene.SceneChildrenBuilder

class TileEditorScene : Scene() {
    override fun SceneChildrenBuilder.build() {
        - TileBitmaskEditView()
    }
}
