package net.arvandor.talekeeper.player

import org.bukkit.OfflinePlayer

@JvmInline
value class TtPlayerId(val value: String) {
    companion object {
        fun ofPlayer(player: OfflinePlayer) = TtPlayerId(player.uniqueId.toString())
    }
}
