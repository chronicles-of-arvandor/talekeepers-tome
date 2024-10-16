package net.arvandor.talekeeper.ability

enum class TtAbility(
    val displayName: String,
    val shortName: String,
) {
    STRENGTH("Strength", "STR"),
    DEXTERITY("Dexterity", "DEX"),
    CONSTITUTION("Constitution", "CON"),
    INTELLIGENCE("Intelligence", "INT"),
    WISDOM("Wisdom", "WIS"),
    CHARISMA("Charisma", "CHA"),
    ;

    companion object {
        fun ofShortName(shortName: String) = values().singleOrNull { it.shortName.equals(shortName, ignoreCase = true) }
    }
}
