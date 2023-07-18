package net.arvandor.talekeeper.choice

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.choice.option.TtChoiceOption
import net.arvandor.talekeeper.choice.option.TtChoiceOptionId
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Choice")
data class TtChoice(
    val id: TtChoiceId,
    val text: String,
    val prerequisites: List<TtPrerequisite>,
    val options: List<TtChoiceOption>,
) : ConfigurationSerializable {

    fun isApplicableFor(character: TtCharacter) = prerequisites.all { it.isMetBy(character) }

    fun getOption(id: TtChoiceOptionId) = options.singleOrNull { it.id == id }

    override fun serialize() = mapOf(
        "id" to id.value,
        "text" to text,
        "prerequisites" to prerequisites,
        "options" to options,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtChoice(
            TtChoiceId(serialized["id"] as String),
            serialized["text"] as String,
            serialized["prerequisites"] as List<TtPrerequisite>,
            serialized["options"] as List<TtChoiceOption>,
        )
    }
}
