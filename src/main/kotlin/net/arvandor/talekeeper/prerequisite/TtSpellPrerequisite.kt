package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.spell.TtSpellId
import net.arvandor.talekeeper.spell.TtSpellService
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SpellPrerequisite")
data class TtSpellPrerequisite(
    val spellIds: List<TtSpellId>,
) : TtPrerequisite {
    override val name: String
        get() {
            val spellService = Services.INSTANCE[TtSpellService::class.java]
            val spells = spellIds.mapNotNull { spellId -> spellService.getSpell(spellId) }
            return "Spells: ${spells.joinToString(", ") { it.name }}"
        }

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.spells.containsAll(spellIds)
    }

    override fun serialize() = mapOf(
        "spell-ids" to spellIds.map { it.value },
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellPrerequisite(
            (serialized["spell-ids"] as List<String>).map(::TtSpellId),
        )
    }
}
