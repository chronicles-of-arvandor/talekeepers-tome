package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SavingThrowProficiencyEffect")
data class TtSavingThrowProficiencyEffect(
    val abilities: List<TtAbility>,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override val name: String
        get() = "Saving throw proficiency: ${abilities.joinToString(", ") { it.displayName }}"

    override fun invoke(character: TtCharacter) = character.copy(savingThrowProficiencies = character.savingThrowProficiencies + abilities)

    override fun serialize() = mapOf(
        "abilities" to abilities.map { it.name },
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSavingThrowProficiencyEffect(
            (serialized["abilities"] as List<String>).map { TtAbility.valueOf(it) },
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
