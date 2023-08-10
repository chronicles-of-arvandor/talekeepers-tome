package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("OrPrerequisite")
data class TtOrPrerequisite(val prerequisites: List<TtPrerequisite>) : TtPrerequisite {
    override val name: String
        get() = prerequisites.joinToString(" OR ") { it.name }

    override fun isMetBy(character: TtCharacter): Boolean {
        return prerequisites.any { it.isMetBy(character) }
    }

    override fun serialize() = mapOf(
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtOrPrerequisite(
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
