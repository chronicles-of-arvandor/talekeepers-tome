package net.arvandor.talekeeper.command.character.context.profile

import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtCharacterContextProfileCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val hideCommand = TtCharacterContextProfileHideCommand(plugin)
    private val unhideCommand = TtCharacterContextProfileUnhideCommand(plugin)

    private val hideAliases = listOf("hide")
    private val unhideAliases = listOf("unhide")

    private val subcommands = hideAliases +
        unhideAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in hideAliases -> hideCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in unhideAliases -> unhideCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /character context profile [${subcommands.joinToString("|")}]")
                true
            }
        }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when {
        args.isEmpty() -> subcommands
        args.size == 1 -> subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        args.size > 1 -> when (args.firstOrNull()?.lowercase()) {
            in hideAliases -> hideCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in unhideAliases -> unhideCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
        else -> emptyList()
    }
}
