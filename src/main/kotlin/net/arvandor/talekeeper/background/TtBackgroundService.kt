package net.arvandor.talekeeper.background

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtBackgroundService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val backgrounds = File(plugin.dataFolder, "backgrounds").listFiles()
        ?.map(::loadBackground)
        ?.associateBy(TtBackground::id)
        ?: emptyMap()

    fun getBackground(id: TtBackgroundId) = backgrounds[id]

    private fun loadBackground(file: File): TtBackground {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("background", TtBackground::class.java)!!
    }
}
