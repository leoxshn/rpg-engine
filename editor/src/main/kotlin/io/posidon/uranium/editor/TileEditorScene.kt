package io.posidon.uranium.editor

import io.posidon.uranium.scene.Scene
import io.posidon.uranium.scene.SceneChildrenBuilder

class TileEditorScene : Scene() {
    override fun SceneChildrenBuilder.build() {
        uiLayer {
            - TileBitmaskEditView()
        }
    }
}
