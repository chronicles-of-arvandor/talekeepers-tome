package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("LevelPrerequisite")
data class TtLevelPrerequisite(private val level: Int) : TtPrerequisite {
    override val name: String
        get() = "Level: $level"

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.classes.values.sumOf { it.level } >= level
    }

    override fun serialize() = mapOf(
        "level" to level,
    )

    companion object {
        @JvmStatic
        fun deserialize(map: Map<String, Any>) = TtLevelPrerequisite(
            level = map["level"] as Int,
        )
    }
}
