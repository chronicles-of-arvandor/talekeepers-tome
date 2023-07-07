package net.arvandor.talekeeper.command.ancestry

import net.arvandor.talekeeper.command.ancestry.traits.TtAncestryTraitsCommand
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtAncestryCommand : CommandExecutor, TabCompleter {

    private val traitsCommand = TtAncestryTraitsCommand()

    private val traitsAliases = listOf("traits")

    private val subcommands = traitsAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (args.firstOrNull()?.lowercase()) {
            in traitsAliases -> traitsCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /ancestry [${subcommands.joinToString("|")}]")
                true
            }
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
            in traitsAliases -> traitsCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
        else -> emptyList()
    }
}
