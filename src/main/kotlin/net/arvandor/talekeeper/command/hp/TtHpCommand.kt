package net.arvandor.talekeeper.command.hp

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.AQUA
import net.md_5.bungee.api.ChatColor.DARK_GREEN
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.YELLOW
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

class TtHpCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val shareCommand = TtHpShareCommand(plugin)
    private val increaseCommand = TtHpIncreaseCommand(plugin)
    private val decreaseCommand = TtHpDecreaseCommand(plugin)
    private val resetCommand = TtHpResetCommand(plugin)

    private val shareAliases = listOf("share")
    private val increaseAliases = listOf("increase", "add", "+")
    private val decreaseAliases = listOf("decrease", "reduce", "subtract", "-")
    private val resetAliases = listOf("reset")

    private val subcommands = shareAliases +
        increaseAliases +
        decreaseAliases +
        resetAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            when (args.first().lowercase()) {
                in shareAliases -> return shareCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
                in increaseAliases -> return increaseCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
                in decreaseAliases -> return decreaseCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
                in resetAliases -> return resetCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            }
        }

        var target: Player? = null
        if (args.isNotEmpty()) {
            if (sender.hasPermission("talekeeper.commands.hp.view.other")) {
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

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}${if (sender == target) "You do" else "${target.name} does"} not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${if (sender == target) "your" else "${target.name}'s"} active character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}${if (sender == target) "You do" else "${target.name} does"} do not currently have an active character.")
                return@asyncTask
            }

            syncTask(plugin) {
                sender.spigot().sendMessage(
                    *buildList {
                        add(
                            TextComponent("HP: ").apply {
                                color = GREEN
                            },
                        )
                        add(
                            TextComponent("${character.hp}").apply {
                                color = YELLOW
                            },
                        )
                        if (character.tempHp > 0) {
                            add(
                                TextComponent("(+${character.tempHp})").apply {
                                    color = AQUA
                                },
                            )
                        }
                        add(
                            TextComponent(" / ").apply {
                                color = ChatColor.GRAY
                            },
                        )
                        add(
                            TextComponent("${character.maxHp}").apply {
                                color = YELLOW
                            },
                        )
                        if (character.tempHp > 0) {
                            add(
                                TextComponent("(+${character.tempHp})").apply {
                                    color = AQUA
                                },
                            )
                        }
                    }.toTypedArray(),
                )
                sender.spigot().sendMessage(
                    *buildList {
                        if (target == sender || sender.hasPermission("talekeeper.commands.hp.set.other")) {
                            if (character.hp + character.tempHp > 0) {
                                add(
                                    TextComponent("[ -50 ]").apply {
                                        color = RED
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to decrease ${if (sender == target) "your" else "${character.name}'s"} HP by 50."),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            if (sender == target) "/hp decrease 50" else "/hp decrease ${target.name} 50",
                                        )
                                    },
                                )
                                add(TextComponent(" "))
                                add(
                                    TextComponent("[ -10 ]").apply {
                                        color = RED
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to decrease ${if (sender == target) "your" else "${character.name}'s"} HP by 10."),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            if (sender == target) "/hp decrease 10" else "/hp decrease ${target.name} 10",
                                        )
                                    },
                                )
                                add(TextComponent(" "))
                                add(
                                    TextComponent("[ -1 ]").apply {
                                        color = RED
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to decrease ${if (sender == target) "your" else "${character.name}'s"} HP by 1."),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            if (sender == target) "/hp decrease" else "/hp decrease ${target.name}",
                                        )
                                    },
                                )
                                add(TextComponent(" "))
                            }
                        }
                        if (target == sender || sender.hasPermission("talekeeper.commands.hp.set.other")) {
                            if (character.hp < character.maxHp) {
                                add(
                                    TextComponent("[ +1 ]").apply {
                                        color = GREEN
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to increase ${if (sender == target) "your" else "${character.name}'s"} HP by 1."),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            if (sender == target) "/hp increase" else "/hp increase ${target.name}",
                                        )
                                    },
                                )
                                add(TextComponent(" "))
                                add(
                                    TextComponent("[ +10 ]").apply {
                                        color = GREEN
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to increase ${if (sender == target) "your" else "${character.name}'s"} HP by 10."),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            if (sender == target) "/hp increase 10" else "/hp increase ${target.name} 10",
                                        )
                                    },
                                )
                                add(TextComponent(" "))
                                add(
                                    TextComponent("[ +50 ]").apply {
                                        color = GREEN
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to increase ${if (sender == target) "your" else "${character.name}'s"} HP by 50."),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            if (sender == target) "/hp increase 50" else "/hp increase ${target.name} 50",
                                        )
                                    },
                                )
                            }
                        }
                    }.toTypedArray(),
                )
                sender.spigot().sendMessage(
                    *buildList {
                        if (target == sender || sender.hasPermission("talekeeper.commands.hp.share.other")) {
                            add(
                                TextComponent("[ Share ]").apply {
                                    color = YELLOW
                                    hoverEvent = HoverEvent(
                                        SHOW_TEXT,
                                        Text("Click here to share ${if (sender == target) "your" else "${character.name}'s"} HP with the party."),
                                    )
                                    clickEvent = ClickEvent(
                                        RUN_COMMAND,
                                        if (sender == target) "/hp share" else "/hp share ${target.name}",
                                    )
                                },
                            )
                            add(TextComponent(" "))
                        }
                        if (target == sender || sender.hasPermission("talekeeper.commands.hp.set.other")) {
                            add(
                                TextComponent("[ Reset ]").apply {
                                    color = DARK_GREEN
                                    hoverEvent = HoverEvent(
                                        SHOW_TEXT,
                                        Text("Click here to reset ${if (sender == target) "your" else "${character.name}'s"} HP to full."),
                                    )
                                    clickEvent = ClickEvent(
                                        RUN_COMMAND,
                                        if (sender == target) "/hp reset" else "/hp reset ${target.name}",
                                    )
                                },
                            )
                        }
                    }.toTypedArray(),
                )
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
        args.isEmpty() -> subcommands + plugin.server.onlinePlayers.map(Player::getName)
        args.size == 1 -> (subcommands + plugin.server.onlinePlayers.map(Player::getName)).filter { it.startsWith(args[0], ignoreCase = true) }
        args.size > 1 -> {
            when (args.first().lowercase()) {
                in shareAliases -> shareCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in increaseAliases -> increaseCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in decreaseAliases -> decreaseCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in resetAliases -> resetCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                else -> emptyList()
            }
        }
        else -> emptyList()
    }
}
