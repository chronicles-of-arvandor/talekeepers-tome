package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("InitiativeBonusEffect")
data class TtInitiativeAbilityModBonusEffect(
    private val ability: TtAbility,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override fun invoke(character: TtCharacter): TtCharacter {
        return character.copy(initiativeBonus = character.initiativeBonus + character.getModifier(ability))
    }

    override fun serialize() = mapOf(
        "ability" to ability.name,
        "prerequisites" to prerequisites,
    )

    companion object {
        fun deserialize(serialized: Map<String, Any>) = TtInitiativeAbilityModBonusEffect(
            (serialized["ability"] as String).let(TtAbility::valueOf),
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
