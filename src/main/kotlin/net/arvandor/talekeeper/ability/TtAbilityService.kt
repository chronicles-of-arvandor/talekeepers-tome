package net.arvandor.talekeeper.ability

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome

class TtAbilityService(private val plugin: TalekeepersTome) : Service {

    override fun getPlugin() = plugin

    private val abilityScoreCost: Map<Int, Int> =
        plugin.config.getConfigurationSection("point-buy.cost")
            ?.getValues(false)
            ?.map { (abilityScore, cost) -> abilityScore.toInt() to cost as Int }
            ?.toMap() ?: emptyMap()

    val maxTotalAbilityCost: Int = plugin.config.getInt("point-buy.max-total")

    private val abilityModifiers: Map<Int, Int> =
        plugin.config.getConfigurationSection("ability-modifiers")
            ?.getValues(false)
            ?.map { (abilityScore, modifier) -> abilityScore.toInt() to modifier as Int }
            ?.toMap()
            ?: emptyMap()

    fun getAbilityScoreCost(score: Int) = abilityScoreCost[score]

    fun getModifier(score: Int) = abilityModifiers[score] ?: 0
}
