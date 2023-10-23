package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("InitiativeAbilityModBonusEffect")
data class TtInitiativeAbilityModBonusEffect(
    private val ability: TtAbility,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override val name: String
        get() = "Initiative ability mod bonus: ${ability.displayName}"

    override fun invoke(character: TtCharacter): TtCharacter {
        return character.copy(initiativeBonus = character.initiativeBonus + character.getModifier(ability))
    }

    override fun serialize() = mapOf(
        "ability" to ability.name,
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtInitiativeAbilityModBonusEffect(
            (serialized["ability"] as String).let(TtAbility::valueOf),
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
