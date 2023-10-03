package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("InitiativeBonusEffect")
data class TtInitiativeBonusEffect(
    private val bonus: Int,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override fun invoke(character: TtCharacter): TtCharacter {
        return character.copy(initiativeBonus = character.initiativeBonus + bonus)
    }

    override fun serialize() = mapOf(
        "bonus" to bonus,
        "prerequisites" to prerequisites,
    )

    companion object {
        fun deserialize(serialized: Map<String, Any>) = TtInitiativeBonusEffect(
            serialized["bonus"] as Int,
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
