package io.posidon.rpgengine.ui.text

import io.posidon.game.shared.types.Vec2f
import io.posidon.rpgengine.gfx.QuadShader
import io.posidon.rpgengine.gfx.assets.Font
import io.posidon.rpgengine.gfx.assets.invoke
import io.posidon.rpgengine.gfx.loadQuadShader
import io.posidon.rpgengine.gfx.renderer.Renderer
import io.posidon.rpgengine.scene.Positional
import io.posidon.rpgengine.scene.node.Node
import io.posidon.rpgengine.util.Stack
import io.posidon.rpgengine.util.set
import io.posidon.rpgengine.window.Window
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import java.nio.IntBuffer

class Text(
    val fontHeight: Float,
    var text: String,
    override var position: Vec2f,
    val font: Font
) : Node(), Positional<Vec2f> {

    private val shader by quadShader("/shaders/text.fsh")

    fun isKerningEnabled() = true

    override fun render(renderer: Renderer, window: Window) {

        font.texture.bind(0)

        val scale = fontHeight / Font.BITMAP_PX_HEIGHT
        val p = window.heightInTiles / window.height
        val factor = Vec2f(1f, 1f).apply { selfDivide(window.contentScale) }
        val m = factor * p * scale
        var lineI = 0

        Stack.push { stack ->
            val pCodePoint = stack.mallocInt(1)
            val x = stack.float(0f)
            val y = stack.float(0f)

            val q = STBTTAlignedQuad.mallocStack(stack.stack)

            var i = 0
            val to = text.length
            while (i < to) {
                i += getCP(text, to, i, pCodePoint)
                val cp = pCodePoint[0]
                if (cp == '\n'.code) {
                    lineI++
                    x[0] = 0f
                    continue
                } else if (cp < 32) {
                    continue
                }
                STBTruetype.stbtt_GetBakedQuad(font.charData, font.texture.width, font.texture.height, cp - 32, x, y, q, true)

                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint)
                    x[0] += STBTruetype.stbtt_GetCodepointKernAdvance(font.info, cp, pCodePoint[0]).toFloat()
                }

                val x0: Float = q.x0() * m.x
                val x1: Float = q.x1() * m.x
                val y0: Float = q.y0() * m.y
                val y1: Float = q.y1() * m.y

                shader {
                    "char_uv_start" set Vec2f(q.s0(), q.t0())
                    "char_uv_end" set Vec2f(q.s1(), q.t1())
                }

                val sp = STBTruetype.stbtt_ScaleForPixelHeight(font.info, fontHeight)

                val yy = lineI * ((font.ascent - font.descent + font.lineGap) * sp + fontHeight / 2f) * p

                renderer.renderQuad(window, QuadShader(shader), position.x + x0 * 2, -(position.y + yy + y0 + fontHeight * p), x1 - x0, y1 - y0)
            }
        }
    }

    private fun getStringWidth(info: STBTTFontinfo, text: String, from: Int, to: Int): Float {
        var width = 0
        Stack.push { stack ->
            val pCodePoint = stack.mallocInt(1)
            val pAdvancedWidth = stack.mallocInt(1)
            val pLeftSideBearing = stack.mallocInt(1)
            var i = from
            while (i < to) {
                i += getCP(text, to, i, pCodePoint)
                val cp = pCodePoint[0]
                STBTruetype.stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing)
                width += pAdvancedWidth[0]
                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint)
                    width += STBTruetype.stbtt_GetCodepointKernAdvance(info, cp, pCodePoint[0])
                }
            }
        }
        return width * STBTruetype.stbtt_ScaleForPixelHeight(info, fontHeight)
    }

    private fun getCP(text: String, to: Int, i: Int, cpOut: IntBuffer): Int {
        val c1 = text[i]
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            val c2 = text[i + 1]
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2))
                return 2
            }
        }
        cpOut.put(0, c1.code)
        return 1
    }
}