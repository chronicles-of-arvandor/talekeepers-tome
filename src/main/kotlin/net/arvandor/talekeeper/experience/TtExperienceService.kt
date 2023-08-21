package net.arvandor.talekeeper.experience

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Sound
import org.bukkit.entity.Player
import kotlin.math.roundToInt

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

    internal fun handleExperienceChange(player: Player, previousExperience: Int, newExperience: Int) {
        if (newExperience > previousExperience) {
            player.sendMessage("$YELLOW+${newExperience - previousExperience} exp")
            val oldLevel = getLevelAtExperience(previousExperience)
            val newLevel = getLevelAtExperience(newExperience)
            if (newLevel > oldLevel) {
                handleLevelChange(player, oldLevel, newLevel)
            } else {
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
            }
        } else if (previousExperience > newExperience) {
            player.sendMessage("$RED-${previousExperience - newExperience} exp")
        }

        val level = getLevelAtExperience(newExperience)
        val experienceSincePreviousLevel = newExperience - getTotalExperienceForLevel(level)
        val experienceForNextLevel = getExperienceForLevel(level + 1)
        val totalExperienceBars = 20
        val filledExperienceBars = ((experienceSincePreviousLevel.toDouble() / experienceForNextLevel.toDouble()) * totalExperienceBars.toDouble()).roundToInt()
        val unfilledExperienceBars = totalExperienceBars - filledExperienceBars
        player.sendMessage("${ChatColor.WHITE}[${YELLOW}${"=".repeat(filledExperienceBars)}${ChatColor.DARK_GRAY}${"=".repeat(unfilledExperienceBars)}${ChatColor.WHITE}] ${YELLOW}${experienceSincePreviousLevel}$GRAY/${YELLOW}$experienceForNextLevel")
    }

    private fun handleLevelChange(player: Player, oldLevel: Int, newLevel: Int) {
        if (newLevel <= oldLevel) return
        player.sendMessage("${GRAY}You have enough experience to level up!")
        player.spigot().sendMessage(
            TextComponent("Click here to level up.").apply {
                color = GREEN
                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to level up!"))
                clickEvent = ClickEvent(RUN_COMMAND, "/character levelup")
            },
        )
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }
}
