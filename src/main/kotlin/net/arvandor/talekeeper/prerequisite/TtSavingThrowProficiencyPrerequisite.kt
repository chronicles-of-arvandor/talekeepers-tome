package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SavingThrowProficiencyPrerequisite")
data class TtSavingThrowProficiencyPrerequisite(
    val abilities: List<TtAbility>,
) : TtPrerequisite {
    override val name: String
        get() = "Saving Throw Proficiency: ${abilities.joinToString(", ") { it.displayName }}"

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.savingThrowProficiencies.containsAll(abilities)
    }

    override fun serialize() = mapOf(
        "abilities" to abilities.map(TtAbility::name),
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSavingThrowProficiencyPrerequisite(
            (serialized["abilities"] as List<String>).map(TtAbility::valueOf),
        )
    }
}
