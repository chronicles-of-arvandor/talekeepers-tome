package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("AndPrerequisite")
data class TtAndPrerequisite(val prerequisites: List<TtPrerequisite>) : TtPrerequisite {
    override val name: String
        get() = prerequisites.joinToString(" AND ") { it.name }

    override fun isMetBy(character: TtCharacter): Boolean {
        return prerequisites.all { it.isMetBy(character) }
    }

    override fun serialize() = mapOf(
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtAndPrerequisite(
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
