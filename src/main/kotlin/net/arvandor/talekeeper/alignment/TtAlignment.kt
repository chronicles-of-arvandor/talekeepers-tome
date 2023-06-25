package net.arvandor.talekeeper.alignment

enum class TtAlignment(
    val displayName: String,
    val acronym: String,
    val order: TtOrder,
    val morality: TtMorality,
) {
    LAWFUL_GOOD("Lawful Good", "LG", TtOrder.LAWFUL, TtMorality.GOOD),
    NEUTRAL_GOOD("Neutral Good", "NG", TtOrder.NEUTRAL, TtMorality.GOOD),
    CHAOTIC_GOOD("Chaotic Good", "CG", TtOrder.CHAOTIC, TtMorality.GOOD),
    LAWFUL_NEUTRAL("Lawful Neutral", "LN", TtOrder.LAWFUL, TtMorality.NEUTRAL),
    NEUTRAL("Neutral", "N", TtOrder.NEUTRAL, TtMorality.NEUTRAL),
    CHAOTIC_NEUTRAL("Chaotic Neutral", "CN", TtOrder.CHAOTIC, TtMorality.NEUTRAL),
    LAWFUL_EVIL("Lawful Evil", "LE", TtOrder.LAWFUL, TtMorality.EVIL),
    NEUTRAL_EVIL("Neutral Evil", "NE", TtOrder.NEUTRAL, TtMorality.EVIL),
    CHAOTIC_EVIL("Chaotic Evil", "CE", TtOrder.CHAOTIC, TtMorality.EVIL),
}
