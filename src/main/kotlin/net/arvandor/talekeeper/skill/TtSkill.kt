package net.arvandor.talekeeper.skill

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.ability.TtAbility.CHARISMA
import net.arvandor.talekeeper.ability.TtAbility.DEXTERITY
import net.arvandor.talekeeper.ability.TtAbility.INTELLIGENCE
import net.arvandor.talekeeper.ability.TtAbility.STRENGTH
import net.arvandor.talekeeper.ability.TtAbility.WISDOM

enum class TtSkill(val ability: TtAbility) {
    ACROBATICS(DEXTERITY),
    ANIMAL_HANDLING(WISDOM),
    ARCANA(INTELLIGENCE),
    ATHLETICS(STRENGTH),
    DECEPTION(CHARISMA),
    HISTORY(INTELLIGENCE),
    INSIGHT(WISDOM),
    INTIMIDATION(CHARISMA),
    INVESTIGATION(INTELLIGENCE),
    MEDICINE(WISDOM),
    NATURE(INTELLIGENCE),
    PERCEPTION(WISDOM),
    PERFORMANCE(CHARISMA),
    PERSUASION(CHARISMA),
    RELIGION(INTELLIGENCE),
    SLEIGHT_OF_HAND(DEXTERITY),
    STEALTH(DEXTERITY),
    SURVIVAL(WISDOM),
}
