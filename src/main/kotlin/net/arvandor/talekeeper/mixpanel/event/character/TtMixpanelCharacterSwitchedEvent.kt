package net.arvandor.talekeeper.mixpanel.event.character

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.OfflinePlayer

data class TtMixpanelCharacterSwitchedEvent(
    private val plugin: TalekeepersTome,
    override val player: OfflinePlayer,
    val oldCharacter: TtCharacter?,
    override val character: TtCharacter?,
) : TtMixpanelCharacterEvent(plugin) {
    override val eventName = "Character Switched"
    override val props: Map<String, Any?>
        get() = characterProps + oldCharacterProps

    val oldCharacterProps: Map<String, Any?>
        get() = mapOf(
            "Old Character" to oldCharacter?.toEventProperties(),
        )
}
