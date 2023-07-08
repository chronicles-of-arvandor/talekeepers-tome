package net.arvandor.talekeeper.ability

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtAbilityService(private val plugin: TalekeepersTome) : Service {

    override fun getPlugin() = plugin

    private val abilityScoreCost: Map<Int, Int>
    val maxTotalAbilityCost: Int

    init {
        val pointBuyConfigFile = File(plugin.dataFolder, "point-buy.yml")
        if (!pointBuyConfigFile.exists()) {
            val pointBuyConfig = YamlConfiguration()
            mapOf(
                8 to 0,
                9 to 1,
                10 to 2,
                11 to 3,
                12 to 4,
                13 to 5,
                14 to 7,
                15 to 9,
            ).forEach { (score, cost) -> pointBuyConfig.set("cost.$score", cost) }
            pointBuyConfig.set("max-total", 27)
            pointBuyConfig.save(pointBuyConfigFile)
        }
        val pointBuyConfig = YamlConfiguration.loadConfiguration(pointBuyConfigFile)
        abilityScoreCost = pointBuyConfig.getConfigurationSection("cost")
            ?.getValues(false)
            ?.map { (abilityScore, cost) -> abilityScore.toInt() to cost as Int }
            ?.toMap() ?: emptyMap()
        maxTotalAbilityCost = pointBuyConfig.getInt("max-total")
    }

    fun getAbilityScoreCost(score: Int) = abilityScoreCost[score]
}
