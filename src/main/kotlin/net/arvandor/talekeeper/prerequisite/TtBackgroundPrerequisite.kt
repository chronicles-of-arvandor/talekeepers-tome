package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("BackgroundPrerequisite")
data class TtBackgroundPrerequisite(val backgroundId: TtBackgroundId) : TtPrerequisite {
    override val name: String
        get() = "Background: ${Services.INSTANCE.get(TtBackgroundService::class.java).getBackground(backgroundId)?.name}"

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.backgroundId == backgroundId
    }

    override fun serialize() = mapOf(
        "background-id" to backgroundId.value,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtBackgroundPrerequisite(
            TtBackgroundId(serialized["background-id"] as String),
        )
    }
}
