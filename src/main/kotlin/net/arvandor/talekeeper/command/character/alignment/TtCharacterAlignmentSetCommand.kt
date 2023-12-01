package net.arvandor.talekeeper.command.character.alignment

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.alignment.TtAlignment
import net.arvandor.talekeeper.alignment.TtMorality
import net.arvandor.talekeeper.alignment.TtOrder
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level

class TtCharacterAlignmentSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        asyncTask(plugin) {
            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}You do not currently have an active character.")
                return@asyncTask
            }

            if (args.isEmpty()) {
                sender.sendMessage("${GRAY}Select alignment:")
                TtMorality.values().forEach { morality ->
                    sender.spigot().sendMessage(
                        *TtOrder.values().flatMap { order ->
                            val alignment = TtAlignment.of(order, morality)
                            listOf(
                                TextComponent(alignment.acronym.padEnd(2)).apply {
                                    color = GREEN
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to set your alignment to ${alignment.displayName}"))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/character alignment set ${alignment.acronym}")
                                },
                                TextComponent(" "),
                            )
                        }.toTypedArray(),
                    )
                }
                return@asyncTask
            }

            val alignment = TtAlignment.fromAcronym(args[0])
            if (alignment == null) {
                sender.sendMessage("${RED}There is no alignment by that acronym.")
                return@asyncTask
            }

            val updatedCharacter = characterService.save(character.copy(alignment = alignment), player = sender).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage(
                "$GRAY================================",
                "${GREEN}Alignment set to ${alignment.displayName}.",
                "$GRAY================================",
            )
            updatedCharacter.display(sender)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when {
        args.isEmpty() -> TtAlignment.values().map { it.acronym }
        args.size == 1 -> TtAlignment.values().map { it.acronym }.filter { it.startsWith(args[0], ignoreCase = true) }
        else -> emptyList()
    }
}
