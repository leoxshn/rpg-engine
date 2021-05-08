package io.posidon.game.netApi.world

enum class Block(
    val id: String
) {

    // Natural
    DIRT("dirt"),
    RED_SAND("red_sand"),
    ROSE_SAND("rose_sand"),
    WHITE_SAND("white_sand"),
    BLACK_SAND("black_sand"),
    STONE("stone"),
    GRAVEL("gravel"),
    MOONSTONE("moonstone"),

    SHARP_STONE("sharp_stone"),
    COPPER_SULFATE("cu_sulfate"),

    // Ores
    COPPER_ORE("cu_ore"),
    RUBY_ORE("ruby_ore"),
    GOLD_ORE("au_ore"),
    ALUMINUM_ORE("aluminum_ore"),
    EMERALD_ORE("emerald_ore"),
    SAPPHIRE_ORE("sapphire_ore"),
    MALACHITE_ORE("malachite_ore"),

    LIGHT_BRICKS("bricks"),

    UNKNOWN("?");

    fun getSaveString(): String {
        return id
    }
}