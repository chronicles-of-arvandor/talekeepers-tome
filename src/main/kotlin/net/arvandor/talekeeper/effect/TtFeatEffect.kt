package net.arvandor.talekeeper.effect

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.feat.TtFeatId
import net.arvandor.talekeeper.feat.TtFeatService
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("FeatEffect")
data class TtFeatEffect(private val feats: List<TtFeatId>, override val prerequisites: List<TtPrerequisite>) : TtEffect {
    override val name: String
        get() {
            val featService = Services.INSTANCE[TtFeatService::class.java]
            return "Feat: ${feats.map { featService.getFeat(it) }
                .joinToString(", ") { feat -> feat?.name ?: "Unknown" }}"
        }

    override fun invoke(character: TtCharacter) = character.copy(feats = character.feats + feats)

    override fun serialize() = mapOf(
        "feats" to feats,
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtFeatEffect(
            (serialized["feats"] as List<String>).map(::TtFeatId),
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
