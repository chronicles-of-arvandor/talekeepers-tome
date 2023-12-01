package net.arvandor.talekeeper.command.hp

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtHpIncreaseCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var target: Player? = null
        if (args.isNotEmpty()) {
            if (sender.hasPermission("talekeeper.commands.hp.set.other")) {
                target = plugin.server.getPlayer(args.first())
            }
        }
        if (target == null) {
            if (sender !is Player) {
                sender.sendMessage("${RED}You must specify a player if using this command from console.")
                return true
            } else {
                target = sender
            }
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

        val amount = args.lastOrNull()?.toIntOrNull() ?: 1

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("$RED${if (sender == target) "You do" else "${target.name} does"} not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${if (sender == target) "your" else "${target.name}'s"} active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("$RED${if (sender == target) "You do" else "${target.name} does"} do not currently have an active character.")
                return@asyncTask
            }

            val cappedHp = (character.hp + amount).coerceAtMost(character.maxHp)
            val cappedAmount = cappedHp - character.hp
            characterService.save(character.copy(hp = cappedHp), player = sender as? Player).onFailure {
                sender.sendMessage("${RED}An error occurred while saving ${if (sender == target) "your" else "${target.name}'s"} character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            sender.sendMessage("$GREEN+$cappedAmount HP")

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
        args.size == 2 -> (1..600).map(Int::toString).filter { args[1].startsWith(it, ignoreCase = true) }
        else -> emptyList()
    }
}
