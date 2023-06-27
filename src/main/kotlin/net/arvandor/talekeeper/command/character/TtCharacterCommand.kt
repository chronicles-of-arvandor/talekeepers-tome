package net.arvandor.talekeeper.command.character

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.command.character.context.TtCharacterContextCommand
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtCharacterCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val contextCommand = TtCharacterContextCommand(plugin)

    private val contextAliases = listOf("context")
    private val subcommands = contextAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (args.firstOrNull()?.lowercase()) {
            in contextAliases -> contextCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /character [${subcommands.joinToString("|")}]")
                true
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        return when {
            args.isEmpty() -> subcommands
            args.size == 1 -> subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
            args.size > 1 -> when (args.firstOrNull()?.lowercase()) {
                in contextAliases -> contextCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
