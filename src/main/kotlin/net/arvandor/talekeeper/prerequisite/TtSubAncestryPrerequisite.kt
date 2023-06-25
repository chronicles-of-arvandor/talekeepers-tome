package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SubAncestryPrerequisite")
data class TtSubAncestryPrerequisite(val ancestryId: TtAncestryId, val subAncestryId: TtSubAncestryId) :
    TtPrerequisite {
    override val name: String
        get() = "Sub-ancestry: ${Services.INSTANCE.get(TtAncestryService::class.java).getAncestry(ancestryId)?.getSubAncestry(subAncestryId)?.name}"

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.ancestryId == ancestryId && character.subAncestryId == subAncestryId
    }

    override fun serialize() = mapOf(
        "ancestry-id" to ancestryId.value,
        "sub-ancestry-id" to subAncestryId.value,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSubAncestryPrerequisite(
            TtAncestryId(serialized["ancestry-id"] as String),
            TtSubAncestryId(serialized["sub-ancestry-id"] as String),
        )
    }
}
