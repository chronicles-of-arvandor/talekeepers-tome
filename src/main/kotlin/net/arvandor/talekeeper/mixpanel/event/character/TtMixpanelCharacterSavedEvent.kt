package net.arvandor.talekeeper.mixpanel.event.character

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import java.util.*

data class TtMixpanelCharacterSavedEvent(
    private val plugin: TalekeepersTome,
    val minecraftUuid: UUID,
    override val character: TtCharacter,
) : TtMixpanelCharacterEvent(plugin) {
    override val distinctId = minecraftUuid.toString()
    override val eventName = "Character Saved"
    override val props: Map<String, Any?>
        get() = characterProps
}
