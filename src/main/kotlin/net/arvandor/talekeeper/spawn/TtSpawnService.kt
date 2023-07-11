package net.arvandor.talekeeper.spawn

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.Location

class TtSpawnService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    var newCharacterSpawn: Location
        get() = plugin.config.getLocation("spawns.new-character")
            ?: plugin.server.worlds.first().spawnLocation
        set(value) {
            plugin.config.set("spawns.new-character", value)
            plugin.saveConfig()
        }
}
