package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("AbilityEffect")
data class TtAbilityEffect(
    private val abilities: Map<TtAbility, Int>,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override fun invoke(character: TtCharacter) = character.copy(
        abilityScoreBonuses = TtAbility.values()
            .associateWith { ability ->
                (character.abilityScoreBonuses[ability] ?: 0) + (abilities[ability] ?: 0)
            },
    )

    override fun serialize() = mapOf(
        "abilities" to abilities.mapKeys { it.key.name },
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtAbilityEffect(
            (serialized["abilities"] as Map<String, Int>).mapKeys { TtAbility.valueOf(it.key) },
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
