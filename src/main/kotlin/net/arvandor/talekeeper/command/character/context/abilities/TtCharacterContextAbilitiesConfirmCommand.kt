package net.arvandor.talekeeper.command.character.context.abilities

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.ability.TtAbilityService
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level

class TtCharacterContextAbilitiesConfirmCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }

        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${RED}No Minecraft profile service was found. Please contact an admin.")
            return true
        }

        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        val abilityService = Services.INSTANCE[TtAbilityService::class.java]
        if (abilityService == null) {
            sender.sendMessage("${RED}No ability service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your character creation context.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            if (!TtAbility.values().all { ability ->
                    val score = ctx.abilityScoreChoices[ability]
                    score != null && score >= 8 && score <= 15
                }
            ) {
                sender.sendMessage("${RED}Some of your ability scores are not within bounds. Please fix them and try again.")
                return@asyncTask
            }

            val scoreCost = TtAbility.values().sumOf { ability ->
                val score = ctx.abilityScoreChoices[ability] ?: 0
                abilityService.getAbilityScoreCost(score) ?: 0
            }
            if (scoreCost > abilityService.maxTotalAbilityCost) {
                sender.sendMessage("${RED}Your overall score cost is too high. Please reduce some of your scores and try again.")
                return@asyncTask
            }

            sender.sendMessage(
                "${ChatColor.GRAY}================================",
                "${ChatColor.GREEN}Ability score choices set",
                "${ChatColor.GRAY}================================",
            )

            ctx.display(sender)
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = emptyList<String>()
}
