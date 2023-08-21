package net.arvandor.talekeeper.language

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ConfigLoadException
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
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getObject("language", TtLanguage::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load language from ${file.name}", failure.reason)
        }
    }
}
