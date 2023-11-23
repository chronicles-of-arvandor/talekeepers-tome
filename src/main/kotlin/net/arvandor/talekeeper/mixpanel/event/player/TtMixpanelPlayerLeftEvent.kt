package net.arvandor.talekeeper.mixpanel.event.player

import net.arvandor.talekeeper.mixpanel.TtMixpanelEvent
import org.bukkit.OfflinePlayer

data class TtMixpanelPlayerLeftEvent(
    override val player: OfflinePlayer,
) : TtMixpanelEvent {
    override val eventName = "Player Left"
    override val props: Map<String, Any?>? = null
}
