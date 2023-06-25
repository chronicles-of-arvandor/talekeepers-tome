package net.arvandor.talekeeper.spell.component

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("MaterialSpellComponent")
data class TtMaterialSpellComponent(
    val text: String,
    val cost: Int,
    val consume: Boolean,
) : ConfigurationSerializable {
    override fun serialize() = mapOf(
        "text" to text,
        "cost" to cost,
        "consume" to consume,
    )
}
