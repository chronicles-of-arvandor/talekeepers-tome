package net.arvandor.talekeeper.command.hp

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level

class TtHpResetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var target: Player? = null
        if (args.isNotEmpty()) {
            if (sender.hasPermission("talekeeper.commands.hp.set.other")) {
                target = plugin.server.getPlayer(args.first())
            }
        }
        if (target == null) {
            if (sender !is Player) {
                sender.sendMessage("${ChatColor.RED}You must specify a player if using this command from console.")
                return true
            } else {
                target = sender
            }
        }

        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${ChatColor.RED}No Minecraft profile service was found. Please contact an admin.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${ChatColor.RED}No character service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${ChatColor.RED}${if (sender == target) "You do" else "${target.name} does"} not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${ChatColor.RED}An error occurred while getting ${if (sender == target) "your" else "${target.name}'s"} active character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${ChatColor.RED}${if (sender == target) "You do" else "${target.name} does"} do not currently have an active character.")
                return@asyncTask
            }

            characterService.save(character.copy(hp = character.maxHp), player = sender as? Player).onFailure {
                sender.sendMessage("${ChatColor.RED}An error occurred while saving ${if (sender == target) "your" else "${target.name}'s"} character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            target.sendMessage("${ChatColor.GREEN}HP reset to ${character.maxHp}.")
            if (sender != target) {
                sender.sendMessage("${ChatColor.GREEN}${character.name}'s HP reset to ${character.maxHp}.")
            }

            syncTask(plugin) {
                plugin.server.dispatchCommand(sender, "hp")
                if (sender != target) {
                    plugin.server.dispatchCommand(target, "hp")
                }
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
        args.isEmpty() -> plugin.server.onlinePlayers.map(Player::getName)
        args.size == 1 -> plugin.server.onlinePlayers.map(Player::getName).filter { it.startsWith(args[0], ignoreCase = true) }
        else -> emptyList()
    }
}
