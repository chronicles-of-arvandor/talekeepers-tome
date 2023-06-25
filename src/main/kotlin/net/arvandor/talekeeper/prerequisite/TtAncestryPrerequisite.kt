package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("AncestryPrerequisite")
data class TtAncestryPrerequisite(val ancestryId: TtAncestryId) : TtPrerequisite {
    override val name: String
        get() = "Ancestry: ${Services.INSTANCE.get(TtAncestryService::class.java).getAncestry(ancestryId)?.name}"

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.ancestryId == ancestryId
    }

    override fun serialize() = mapOf(
        "ancestry-id" to ancestryId.value,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtAncestryPrerequisite(
            TtAncestryId(serialized["ancestry-id"] as String),
        )
    }
}
