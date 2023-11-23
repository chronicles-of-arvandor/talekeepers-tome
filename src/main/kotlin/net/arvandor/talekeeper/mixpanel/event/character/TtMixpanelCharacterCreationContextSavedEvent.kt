package net.arvandor.talekeeper.mixpanel.event.character

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterCreationContext
import org.bukkit.OfflinePlayer

data class TtMixpanelCharacterCreationContextSavedEvent(
    private val plugin: TalekeepersTome,
    override val player: OfflinePlayer,
    override val creationContext: TtCharacterCreationContext,
) : TtMixpanelCharacterCreationContextEvent(plugin) {
    override val eventName = "Character Creation Context Saved"
    override val props: Map<String, Any?>
        get() = contextProps
}
