package net.arvandor.talekeeper.spell.scaling

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SpellScalingLevelDice")
data class TtSpellScalingLevelDice(
    val label: String,
    val scaling: Map<Int, String>,
) : ConfigurationSerializable {
    fun getRollAt(level: Int): String? = if (level >= 1) {
        scaling[level] ?: getRollAt(level - 1)
    } else {
        null
    }

    override fun serialize() = mapOf(
        "label" to label,
        "scaling" to scaling,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellScalingLevelDice(
            serialized["label"] as String,
            (serialized["scaling"] as Map<String, String>).mapKeys { (key, _) -> key.toInt() },
        )
    }
}
