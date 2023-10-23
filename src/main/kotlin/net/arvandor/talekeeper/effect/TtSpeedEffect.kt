package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SpeedEffect")
data class TtSpeedEffect(
    val speed: Int,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override val name: String
        get() = "Speed bonus: $speed"

    override fun invoke(character: TtCharacter): TtCharacter {
        return character.copy(speed = character.speed.copy(value = character.speed.value + speed))
    }

    override fun serialize() = mapOf(
        "speed" to speed,
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>): TtSpeedEffect {
            return TtSpeedEffect(
                serialized["speed"] as Int,
                serialized["prerequisites"] as List<TtPrerequisite>,
            )
        }
    }
}
