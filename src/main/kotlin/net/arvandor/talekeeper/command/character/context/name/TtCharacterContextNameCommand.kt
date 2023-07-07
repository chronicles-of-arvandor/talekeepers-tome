package net.arvandor.talekeeper.command.character.context.name

import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtCharacterContextNameCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val setCommand = TtCharacterContextNameSetCommand(plugin)
    private val hideCommand = TtCharacterContextNameHideCommand(plugin)
    private val unhideCommand = TtCharacterContextNameUnhideCommand(plugin)

    private val setAliases = listOf("set")
    private val hideAliases = listOf("hide")
    private val unhideAliases = listOf("unhide")

    private val subcommands = setAliases +
        hideAliases +
        unhideAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in setAliases -> setCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in hideAliases -> hideCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in unhideAliases -> unhideCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /character context name [${subcommands.joinToString("|")}]")
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
        args.size > 1 -> when (args.first().lowercase()) {
            in setAliases -> setCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in hideAliases -> hideCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in unhideAliases -> unhideCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
        else -> emptyList()
    }
}
