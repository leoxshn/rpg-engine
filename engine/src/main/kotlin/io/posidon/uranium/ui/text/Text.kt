package io.posidon.uranium.ui.text

import io.posidon.uranium.util.ConsoleColors
import io.posidon.uranium.mathlib.types.Vec2f
import io.posidon.uranium.mathlib.types.Vec4f
import io.posidon.uranium.debug.MainLogger
import io.posidon.uranium.gfx.Context
import io.posidon.uranium.gfx.assets.Font
import io.posidon.uranium.gfx.assets.Texture
import io.posidon.uranium.gfx.assets.invoke
import io.posidon.uranium.gfx.renderer.Renderer
import io.posidon.uranium.gfx.renderer.renderQuad2D
import io.posidon.uranium.scene.Positional
import io.posidon.uranium.scene.node.Node
import io.posidon.uranium.ui.UIComponent
import io.posidon.uranium.util.Stack
import io.posidon.uranium.util.fromColorInt
import io.posidon.uranium.util.set
import io.posidon.uranium.window.Window
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import java.nio.IntBuffer
import kotlin.random.Random

class Text(
    val fontHeight: Float,
    var text: String,
    override val position: Vec2f,
    val font: Font
) : Node(), UIComponent {

    private val shader by objectShader("/uraniumEngine/shaders/text.fsh")
    private val symbolShader by objectShader("/uraniumEngine/shaders/text_symbol.fsh")

    fun isKerningEnabled() = true

    val lineSpacingMultiplier = 1f
    var textColor = Vec4f.fromColorInt(0xffdddddd.toInt())

    private val symbolMap = HashMap<UShort, Texture>()

    fun customSymbol(context: Context, log: MainLogger, path: String) = customSymbol(context.loadTexture(log, path))
    fun customSymbol(texture: Texture): CustomSymbol {
        return CustomSymbol(generateSymbolID()).also { symbolMap[it.id] = texture }
    }

    private tailrec fun generateSymbolID(): UShort {
        val id = Random.nextInt().toUShort()
        return if (symbolMap.contains(id)) generateSymbolID() else id
    }

    @JvmInline
    value class CustomSymbol(val id: UShort) {
        override fun toString() = "$ESCAPE$CUSTOM_SYMBOL_ESCAPE${id.toString(16).padStart(4, '0')}"
    }


    private var sp: Float = 0f

    override fun init() {
        sp = STBTruetype.stbtt_ScaleForPixelHeight(font.info, fontHeight)
    }

    override fun render(renderer: Renderer, window: Window) {
        val text = text
        var textColor = textColor

        font.texture.bind(0)

        val scale = fontHeight / Font.BITMAP_PX_HEIGHT
        val factor = Vec2f(1f, 1f).apply { selfDivide(window.contentScale) }
        val m = factor * scale
        var lineI = 0

        Stack.push { stack ->
            val pCodePoint = stack.mallocInt(1)
            val x = stack.float(0f)
            val y = stack.float(0f)

            val q = STBTTAlignedQuad.mallocStack(stack.stack)

            var i = 0
            val to = text.length
            loop@ while (i < to) {
                if (text[i] == ConsoleColors.ESCAPE) {
                    when {
                        text[i + 1] == COLOR_ESCAPE -> {
                            val hex = text.substring(i + 2, i + 2 + 8)
                            textColor = Vec4f.fromColorInt(hex.toLong(16).toInt())
                            i += 2 + 8
                            continue@loop
                        }
                        text[i + 1] == COLOR_RESET -> {
                            textColor = this.textColor
                            i += 2
                            continue@loop
                        }
                        text[i + 1] == CUSTOM_SYMBOL_ESCAPE -> {
                            val id = text.substring(i + 2, i + 2 + 4)
                            val symbol = symbolMap[id.toUInt(16).toUShort()]
                            i += 2 + 4
                            symbol ?: continue@loop

                            symbol.bind(0)

                            symbolShader {
                                "text_color" set textColor
                            }

                            val yy = lineI * ((font.ascent - font.descent + font.lineGap) * sp + fontHeight * lineSpacingMultiplier)

                            val width = font.ascent * symbol.width / symbol.height * factor.x * sp
                            val height = font.ascent * factor.y * sp
                            val px = position.x + x[0] * 2 * m.x
                            val py = -(position.y + yy + y[0] * m.y + fontHeight * factor.y) + height / 2f
                            renderer.renderQuad2D(window, symbolShader, px, py, width, height)

                            x[0] += width / m.y / 2f

                            font.texture.bind(0)
                            continue@loop
                        }
                    }
                }
                i += getCP(text, to, i, pCodePoint)
                val cp = pCodePoint[0]
                if (cp == '\n'.code) {
                    lineI++
                    x[0] = 0f
                    continue
                } else if (cp < 32) {
                    continue
                }
                STBTruetype.stbtt_GetBakedQuad(
                    font.charData,
                    font.texture.width,
                    font.texture.height,
                    cp - 32,
                    x,
                    y,
                    q,
                    true
                )

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
                    "text_color" set textColor
                }

                val yy = lineI * ((font.ascent - font.descent + font.lineGap) * sp + fontHeight * lineSpacingMultiplier)

                renderer.renderQuad2D(window, shader, position.x + x1 * 2, -(position.y + yy + y0 + fontHeight), x1 - x0, y1 - y0)
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

    override fun getWidth(): Float {
        return text.lines().maxOf { getStringWidth(font.info, it, 0, it.length) }
    }

    override fun getHeight(): Float {
        return text.lines().size * ((font.ascent - font.descent + font.lineGap) * sp + fontHeight * lineSpacingMultiplier)
    }

    companion object {
        const val ESCAPE = ConsoleColors.ESCAPE
        const val COLOR_ESCAPE = '#'
        const val COLOR_RESET = '!'
        const val CUSTOM_SYMBOL_ESCAPE = '¿'
    }
}

inline fun c(color: UInt, string: String) = "${Text.ESCAPE}${Text.COLOR_ESCAPE}${color.toString(16).padStart(8, '0')}$string${Text.ESCAPE}${Text.COLOR_RESET}"
inline fun c(color: Int, string: String) = c(color.toUInt(), string)
inline fun c(color: Long, string: String) = c(color.toUInt(), string)