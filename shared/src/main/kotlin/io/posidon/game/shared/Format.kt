package io.posidon.game.shared

object Format {
    inline fun escapeChar(string: String, orig: Char, new: Char) = string
        .replace("\\", "\\\\")
        .replace("$orig", "\\$new")

    inline fun unescapeChar(string: String, orig: Char, new: Char) = string
        .replace(Regex("(?<=(?<!\\)\\(\\\\))*\\$orig"), "$new")
        .replace("\\\\", "\\")

    inline fun newLineEscape(string: String) = escapeChar(string, '\n', 'n')
    inline fun newLineUnescape(string: String) = unescapeChar(string, 'n', '\n')

    inline fun doubleQuotesEscape(string: String) = escapeChar(string, '"', '"')
    inline fun doubleQuotesUnescape(string: String) = unescapeChar(string, '"', '"')

    inline fun pointer(p: Int) = "0x" + p.toString(16)
    inline fun pointer(p: Long) = "0x" + p.toString(16)
}