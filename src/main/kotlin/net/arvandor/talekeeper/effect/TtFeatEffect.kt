package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.feat.TtFeatId
import net.arvandor.talekeeper.prerequisite.TtPrerequisite

data class TtFeatEffect(private val feats: List<TtFeatId>, override val prerequisites: List<TtPrerequisite>) : TtEffect {
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
