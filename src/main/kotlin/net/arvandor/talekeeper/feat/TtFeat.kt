package net.arvandor.talekeeper.feat

import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import net.arvandor.talekeeper.source.TtSource
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Feat")
data class TtFeat(
    val id: TtFeatId,
    val name: String,
    val source: String,
    val page: Int,
    val otherSources: List<TtSource>,
    val prerequisites: List<TtPrerequisite>,

) : ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "id" to id.value,
            "name" to name,
            "source" to source,
            "page" to page,
            "other-sources" to otherSources,
            "prerequisites" to prerequisites,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>): TtFeat {
            return TtFeat(
                id = (serialized["id"] as String).let(::TtFeatId),
                name = serialized["name"] as String,
                source = serialized["source"] as String,
                page = serialized["page"] as Int,
                otherSources = serialized["other-sources"] as List<TtSource>,
                prerequisites = serialized["prerequisites"] as List<TtPrerequisite>,
            )
        }
    }
}
