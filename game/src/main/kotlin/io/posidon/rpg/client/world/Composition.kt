package io.posidon.rpg.client.world

/**
 * Percentages of elements (1f = 1%)
 */
data class Composition(
    val redMatter: Float, // energy conduction
    val greenMatter: Float, // energy production, silicone
    val blueMatter: Float, // energy production
    val stringMatter: Float, // building
) {
    init {
        val sum = (greenMatter * 10f + redMatter * 10f + stringMatter * 10f + blueMatter * 10f) / 10f
        if (sum != 100f)
            throw IllegalStateException("All the elements in a composition must add up to 100, but the sum is $sum, (r: $redMatter, g: $greenMatter, b: $blueMatter, s: $stringMatter)")
    }
}
