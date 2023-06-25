package net.arvandor.talekeeper.feat

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
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
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("feat", TtFeat::class.java)!!
    }
}
