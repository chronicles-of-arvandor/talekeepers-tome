package net.arvandor.talekeeper.experience

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome

class TtExperienceService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val levelExperience = plugin.config.getConfigurationSection("experience.levels")
        ?.getValues(false)
        ?.mapKeys { (key, _) -> key.toInt() }
        ?.mapValues { (_, value) -> value as Int }
        ?: emptyMap()

    fun getLevelAtExperience(experience: Int): Int {
        return levelExperience.entries
            .filter { (_, requiredExperience) -> experience >= requiredExperience }
            .maxOf { (level, _) -> level }
    }

    fun getExperienceForLevel(level: Int): Int {
        if (level <= 1) return getTotalExperienceForLevel(level)
        return getTotalExperienceForLevel(level) - getTotalExperienceForLevel(level - 1)
    }

    fun getTotalExperienceForLevel(level: Int): Int {
        return levelExperience[level] ?: 0
    }

    fun getMaxLevel(): Int {
        return levelExperience.keys.max()
    }
}
