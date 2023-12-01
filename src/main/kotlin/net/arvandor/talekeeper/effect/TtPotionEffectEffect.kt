package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.potion.PotionEffect

@SerializableAs("PotionEffectEffect")
data class TtPotionEffectEffect(
    val potionEffect: PotionEffect,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override val name: String
        get() = "Potion Effect: ${potionEffect.type.name} ${potionEffect.amplifier + 1} ${if (potionEffect.isInfinite) "forever" else "for ${potionEffect.duration / 20} seconds"}"

    override fun invoke(character: TtCharacter): TtCharacter {
        return character.copy(potionEffects = character.potionEffects + potionEffect)
    }

    override fun serialize() = mapOf(
        "potion-effect" to potionEffect,
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtPotionEffectEffect(
            serialized["potion-effect"] as PotionEffect,
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
