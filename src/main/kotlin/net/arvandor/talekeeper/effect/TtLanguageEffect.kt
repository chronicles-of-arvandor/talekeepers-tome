package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.language.TtLanguageId
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("LanguageEffect")
data class TtLanguageEffect(
    private val languages: List<TtLanguageId>,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override fun invoke(character: TtCharacter) = character.copy(languages = character.languages + languages)

    override fun serialize() = mapOf(
        "languages" to languages.map(TtLanguageId::value),
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtLanguageEffect(
            (serialized["languages"] as List<String>).map(::TtLanguageId),
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
