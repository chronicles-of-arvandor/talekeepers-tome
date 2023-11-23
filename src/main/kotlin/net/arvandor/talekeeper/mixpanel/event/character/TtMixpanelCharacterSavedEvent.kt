package net.arvandor.talekeeper.mixpanel.event.character

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.OfflinePlayer

data class TtMixpanelCharacterSavedEvent(
    private val plugin: TalekeepersTome,
    override val player: OfflinePlayer,
    override val character: TtCharacter,
) : TtMixpanelCharacterEvent(plugin) {
    override val eventName = "Character Saved"
    override val props: Map<String, Any?>
        get() = characterProps
}
