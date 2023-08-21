package net.arvandor.talekeeper.feat

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ConfigLoadException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtFeatService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val feats = File(plugin.dataFolder, "feats").listFiles()
        ?.map(::loadFeat)
        ?.associateBy(TtFeat::id)
        ?: emptyMap()

    fun getFeat(id: TtFeatId): TtFeat? = feats[id]

    private fun loadFeat(file: File): TtFeat {
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getObject("feat", TtFeat::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load feat from ${file.name}", failure.reason)
        }
    }
}
