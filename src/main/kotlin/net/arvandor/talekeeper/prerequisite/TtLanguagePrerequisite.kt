package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.language.TtLanguageId
import net.arvandor.talekeeper.language.TtLanguageService
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("LanguagePrerequisite")
data class TtLanguagePrerequisite(
    val languageIds: List<TtLanguageId>,
) : TtPrerequisite {
    override val name: String
        get() {
            val languageService = Services.INSTANCE[TtLanguageService::class.java]
            val languages = languageIds.mapNotNull { languageId -> languageService.getLanguage(languageId) }
            return "Languages: ${languages.joinToString(", ") { it.name }}"
        }

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.languages.containsAll(languageIds)
    }

    override fun serialize() = mapOf(
        "language-ids" to languageIds.map { it.value },
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtLanguagePrerequisite(
            (serialized["language-ids"] as List<String>).map(::TtLanguageId),
        )
    }
}
