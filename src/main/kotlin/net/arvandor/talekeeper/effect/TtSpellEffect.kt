package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import net.arvandor.talekeeper.spell.TtSpellId
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SpellEffect")
data class TtSpellEffect(
    private val spells: List<TtSpellId>,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override fun invoke(character: TtCharacter) = character.copy(
        spells = character.spells + spells,
    )

    override fun serialize() = mapOf(
        "spells" to spells.map(TtSpellId::value),
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellEffect(
            (serialized["spells"] as List<String>).map(::TtSpellId),
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
