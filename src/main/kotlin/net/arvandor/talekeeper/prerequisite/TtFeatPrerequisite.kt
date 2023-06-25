package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.feat.TtFeatId
import net.arvandor.talekeeper.feat.TtFeatService

data class TtFeatPrerequisite(private val featId: TtFeatId) : TtPrerequisite {
    override val name: String
        get() = "Feat: ${Services.INSTANCE.get(TtFeatService::class.java).getFeat(featId)?.name}"

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.feats.contains(featId)
    }

    override fun serialize() = mapOf(
        "feat-id" to featId.value,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtFeatPrerequisite(
            TtFeatId(serialized["feat-id"] as String),
        )
    }
}
