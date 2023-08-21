package net.arvandor.talekeeper.command.experience

import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtExperienceCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val addCommand = TtExperienceAddCommand(plugin)
    private val setCommand = TtExperienceSetCommand(plugin)
    private val viewCommand = TtExperienceViewCommand(plugin)

    private val addAliases = listOf("add")
    private val setAliases = listOf("set")
    private val viewAliases = listOf("view")

    private val subcommands = addAliases + setAliases + viewAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in addAliases -> addCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in setAliases -> setCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in viewAliases -> viewCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /experience [${subcommands.joinToString("|")}]")
                true
            }
        }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when (args.size) {
        0 -> subcommands
        1 -> subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        else -> when (args.firstOrNull()?.lowercase()) {
            in addAliases -> addCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in setAliases -> setCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in viewAliases -> viewCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
    }
}
