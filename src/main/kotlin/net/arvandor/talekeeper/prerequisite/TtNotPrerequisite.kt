package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("NotPrerequisite")
data class TtNotPrerequisite(val prerequisite: TtPrerequisite) : TtPrerequisite {
    override val name: String
        get() = "NOT " + prerequisite.name

    override fun isMetBy(character: TtCharacter): Boolean {
        return !prerequisite.isMetBy(character)
    }

    override fun serialize() = mapOf(
        "prerequisite" to prerequisite,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtNotPrerequisite(
            serialized["prerequisite"] as TtPrerequisite,
        )
    }
}
