package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("JackOfAllTradesEffect")
data class TtJackOfAllTradesEffect(
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override fun invoke(character: TtCharacter): TtCharacter {
        return character.copy(jackOfAllTrades = true)
    }

    override fun serialize() = mapOf(
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtJackOfAllTradesEffect(
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
