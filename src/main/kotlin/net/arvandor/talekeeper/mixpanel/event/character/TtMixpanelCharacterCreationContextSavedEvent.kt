package net.arvandor.talekeeper.mixpanel.event.character

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterCreationContext
import java.util.*

data class TtMixpanelCharacterCreationContextSavedEvent(
    private val plugin: TalekeepersTome,
    val minecraftUuid: UUID,
    override val creationContext: TtCharacterCreationContext,
) : TtMixpanelCharacterCreationContextEvent(plugin) {
    override val distinctId = minecraftUuid.toString()
    override val eventName = "Character Creation Context Saved"
    override val props: Map<String, Any?>
        get() = contextProps
}
