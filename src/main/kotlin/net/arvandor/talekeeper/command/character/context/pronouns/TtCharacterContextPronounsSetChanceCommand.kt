package net.arvandor.talekeeper.command.character.context.pronouns

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.pronouns.TtPronounSetId
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
import java.text.DecimalFormat
import java.util.logging.Level

class TtCharacterContextPronounsSetChanceCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

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

        val pronounService = Services.INSTANCE[TtPronounService::class.java]
        if (pronounService == null) {
            sender.sendMessage("${RED}No pronoun service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            var ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}You do not have a character creation context. Please contact an admin.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            if (args.isEmpty()) {
                sender.sendMessage("${RED}You must specify a pronoun set ID.")
                return@asyncTask
            }

            val pronounSetId = TtPronounSetId(args[0])
            if (!ctx.pronouns.containsKey(pronounSetId)) {
                sender.sendMessage("${RED}Invalid pronoun set ID.")
                return@asyncTask
            }

            val pronounSet = pronounService.get(pronounSetId)
            if (pronounSet == null) {
                sender.sendMessage("${RED}Invalid pronoun set ID.")
                return@asyncTask
            }

            if (args.size >= 2) {
                val chance = args[1].toIntOrNull()
                if (chance == null) {
                    sender.sendMessage("${RED}Chance must be an integer.")
                    return@asyncTask
                }

                ctx = characterService.save(
                    ctx.copy(
                        pronouns = ctx.pronouns + (pronounSetId to chance),
                    ),
                ).onFailure {
                    sender.sendMessage("${RED}Failed to save character creation context. Please contact an admin.")
                    return@asyncTask
                }
            }

            val currentChance = ctx.pronouns[pronounSetId]
            if (currentChance == null) {
                sender.sendMessage("${RED}You are not currently using that pronoun set.")
                return@asyncTask
            }

            val currentTotalChance = ctx.pronouns.values.sum()
            val decimalFormat = DecimalFormat("#.##")

            val currentPercentageChance = (currentChance.toDouble() / currentTotalChance.toDouble()) * 100.0
            sender.spigot().sendMessage(
                *buildList {
                    if (currentChance > 1) {
                        add(
                            TextComponent("<--").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to reduce the chance of ${pronounSet.name}"))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character context pronouns setchance ${pronounSetId.value} ${currentChance - 1}")
                            },
                        )
                    }
                    add(
                        TextComponent(
                            " $currentChance/$currentTotalChance (${decimalFormat.format(
                                currentPercentageChance,
                            )}%) ",
                        ).apply {
                            color = GRAY
                        },
                    )
                    add(
                        TextComponent("-->").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to increase the chance of ${pronounSet.name}"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context pronouns setchance ${pronounSetId.value} ${currentChance + 1}")
                        },
                    )
                }.toTypedArray(),
            )

            sender.spigot().sendMessage(
                TextComponent("< Back").apply {
                    color = GREEN
                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to go back to your profile"))
                    clickEvent = ClickEvent(RUN_COMMAND, "/character context")
                },
            )
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val pronounService = Services.INSTANCE[TtPronounService::class.java] ?: return emptyList()
        return when {
            args.isEmpty() -> pronounService.getAll()
                .onFailure { return emptyList() }
                .map { it.id.value }
            args.size == 1 -> pronounService.getAll()
                .onFailure { return emptyList() }
                .map { it.id.value }
                .filter { it.startsWith(args[0], ignoreCase = true) }
            else -> emptyList()
        }
    }
}
