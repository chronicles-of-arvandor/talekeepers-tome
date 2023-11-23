package net.arvandor.talekeeper.mixpanel

import org.bukkit.OfflinePlayer

interface TtMixpanelEvent {

    val player: OfflinePlayer?
    val eventName: String
    val props: Map<String, Any?>?
}
