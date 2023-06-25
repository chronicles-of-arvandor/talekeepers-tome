package net.arvandor.talekeeper.language

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtLanguageService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val languages = File(plugin.dataFolder, "languages").listFiles()
        ?.map(::loadLanguage)
        ?.associateBy(TtLanguage::id)
        ?: emptyMap()

    fun getLanguage(id: TtLanguageId) = languages[id]

    private fun loadLanguage(file: File): TtLanguage {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("language", TtLanguage::class.java)!!
    }
}
