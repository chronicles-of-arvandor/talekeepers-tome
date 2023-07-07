package net.arvandor.talekeeper.command.character.context.subancestry

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
import net.arvandor.talekeeper.ancestry.gui.TtSubAncestrySelectionGui
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level

class TtCharacterContextSubAncestrySetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
        if (ancestryService == null) {
            sender.sendMessage("${RED}No ancestry service was found. Please contact an admin.")
            return true
        }

        if (args.isEmpty()) {
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

                val ancestry = ctx.ancestryId?.let(ancestryService::getAncestry)
                if (ancestry == null) {
                    sender.sendMessage("${RED}You need an ancestry to select a sub-ancestry.")
                    return@asyncTask
                }

                syncTask(plugin) {
                    val subAncestrySelectionGui = TtSubAncestrySelectionGui(plugin, ancestry)
                    sender.openInventory(subAncestrySelectionGui.inventory)
                }
            }
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

            val ancestry = ctx.ancestryId?.let(ancestryService::getAncestry)
            if (ancestry == null) {
                sender.sendMessage("${RED}You need an ancestry to select a sub-ancestry.")
                return@asyncTask
            }

            val subAncestry = ancestry.getSubAncestry(TtSubAncestryId(args[0]))
            if (subAncestry == null) {
                sender.sendMessage("${RED}No sub-ancestry found with ID ${args[0]}")
                return@asyncTask
            }

            val updatedCtx = characterService.save(ctx.copy(subAncestryId = subAncestry.id)).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character creation context.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage(
                "$GRAY================================",
                "${GREEN}Sub-ancestry set to ${subAncestry.name}.",
                "$GRAY================================",
            )

            updatedCtx.display(sender)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
        return when {
            args.isEmpty() -> ancestryService.getAll().map { it.id.value }
            args.size == 1 -> ancestryService.getAll().map { it.id.value }.filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
    }
}
