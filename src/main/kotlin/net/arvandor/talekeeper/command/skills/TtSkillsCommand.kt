package net.arvandor.talekeeper.command.skills

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.arvandor.talekeeper.skill.gui.TtSkillProficiencyGui
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level

class TtSkillsCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }
        val target = if (args.isEmpty() || !sender.hasPermission("talekeeper.commands.skills.other")) {
            sender
        } else {
            plugin.server.getOfflinePlayer(args[0])
        }

        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${RED}No Minecraft profile service was found. Please contact an admin.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}That player does not have a Minecraft profile. Have they logged in before?")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${if (sender == target) "your" else "${target.name}'s"} active character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}That player does not have an active character.")
                return@asyncTask
            }

            syncTask(plugin) {
                val skillProficiencyGui = TtSkillProficiencyGui(plugin, character)
                sender.openInventory(skillProficiencyGui.inventory)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when {
        args.isEmpty() -> plugin.server.offlinePlayers.map(OfflinePlayer::getName)
        args.size == 1 -> plugin.server.offlinePlayers.mapNotNull(OfflinePlayer::getName)
            .filter { it.startsWith(args[0], ignoreCase = true) }
        else -> emptyList()
    }
}
