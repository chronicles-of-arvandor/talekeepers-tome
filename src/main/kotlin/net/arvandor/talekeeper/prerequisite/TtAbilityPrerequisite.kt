package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("AbilityPrerequisite")
data class TtAbilityPrerequisite(
    val ability: TtAbility,
    val score: Int,
) : TtPrerequisite {
    override val name: String
        get() = "Ability score: $score ${ability.displayName}"

    override fun isMetBy(character: TtCharacter): Boolean {
        return (character.abilityScores[ability] ?: 0) >= score
    }

    override fun serialize() = mapOf(
        "ability" to ability.name,
        "score" to score,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtAbilityPrerequisite(
            ability = TtAbility.valueOf(serialized["ability"] as String),
            score = serialized["score"] as Int,
        )
    }
}
