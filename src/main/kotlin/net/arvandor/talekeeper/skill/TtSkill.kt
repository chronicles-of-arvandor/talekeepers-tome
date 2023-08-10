package net.arvandor.talekeeper.skill

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.ability.TtAbility.CHARISMA
import net.arvandor.talekeeper.ability.TtAbility.DEXTERITY
import net.arvandor.talekeeper.ability.TtAbility.INTELLIGENCE
import net.arvandor.talekeeper.ability.TtAbility.STRENGTH
import net.arvandor.talekeeper.ability.TtAbility.WISDOM

enum class TtSkill(val displayName: String, val ability: TtAbility) {
    ACROBATICS("Acrobatics", DEXTERITY),
    ANIMAL_HANDLING("Animal Handling", WISDOM),
    ARCANA("Arcana", INTELLIGENCE),
    ATHLETICS("Athletics", STRENGTH),
    DECEPTION("Deception", CHARISMA),
    HISTORY("History", INTELLIGENCE),
    INSIGHT("Insight", WISDOM),
    INTIMIDATION("Intimidation", CHARISMA),
    INVESTIGATION("Investigation", INTELLIGENCE),
    MEDICINE("Medicine", WISDOM),
    NATURE("Nature", INTELLIGENCE),
    PERCEPTION("Perception", WISDOM),
    PERFORMANCE("Performance", CHARISMA),
    PERSUASION("Persuasion", CHARISMA),
    RELIGION("Religion", INTELLIGENCE),
    SLEIGHT_OF_HAND("Sleight of Hand", DEXTERITY),
    STEALTH("Stealth", DEXTERITY),
    SURVIVAL("Survival", WISDOM),
}
