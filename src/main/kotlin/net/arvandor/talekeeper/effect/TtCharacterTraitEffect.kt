package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import net.arvandor.talekeeper.trait.TtCharacterTrait
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("CharacterTraitEffect")
data class TtCharacterTraitEffect(
    private val traits: List<TtCharacterTrait>,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override val name: String
        get() = "Character trait: ${traits.joinToString(", ") { it.name }}"

    override fun invoke(character: TtCharacter) = character.copy(
        traits = character.traits + traits,
    )

    override fun serialize() = mapOf(
        "traits" to traits,
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtCharacterTraitEffect(
            serialized["traits"] as List<TtCharacterTrait>,
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
