package net.arvandor.talekeeper.command.choice

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.command.choice.list.TtChoiceListCommand
import net.arvandor.talekeeper.command.choice.reset.TtChoiceResetCommand
import net.arvandor.talekeeper.command.choice.select.TtChoiceSelectCommand
import net.arvandor.talekeeper.command.choice.view.TtChoiceViewCommand
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtChoiceCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val listCommand = TtChoiceListCommand(plugin)
    private val viewCommand = TtChoiceViewCommand(plugin)
    private val selectCommand = TtChoiceSelectCommand(plugin)
    private val resetCommand = TtChoiceResetCommand(plugin)

    private val listAliases = listOf("list")
    private val viewAliases = listOf("view", "show")
    private val selectAliases = listOf("select", "choose")
    private val resetAliases = listOf("reset")

    private val subcommands = listAliases + viewAliases + selectAliases + resetAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in listAliases -> listCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in viewAliases -> viewCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in selectAliases -> selectCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in resetAliases -> resetCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /choice [${subcommands.joinToString("|")}]")
                true
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
            else -> when (args.first().lowercase()) {
                in listAliases -> listCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in viewAliases -> viewCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in selectAliases -> selectCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in resetAliases -> resetCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                else -> emptyList()
            }
        }
    }
}
