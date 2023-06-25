package net.arvandor.talekeeper.choice.option

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Option")
class TtChoiceOption(
    val id: TtChoiceOptionId,
    val text: String,
) : ConfigurationSerializable {

    override fun serialize() = mapOf(
        "id" to id.value,
        "text" to text,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtChoiceOption(
            (serialized["id"] as String).let(::TtChoiceOptionId),
            serialized["text"] as String,
        )
    }
}
