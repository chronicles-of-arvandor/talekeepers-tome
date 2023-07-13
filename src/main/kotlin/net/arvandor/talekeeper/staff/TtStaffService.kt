package net.arvandor.talekeeper.staff

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import java.util.*

class TtStaffService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    fun getStaff() = plugin.config.getStringList("staff").map { uuid -> plugin.server.getOfflinePlayer(UUID.fromString(uuid)) }
}
