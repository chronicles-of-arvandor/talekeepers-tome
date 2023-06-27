package net.arvandor.talekeeper.command.character.context.pronouns

import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtCharacterContextPronounsCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val addCommand = TtCharacterContextPronounsAddCommand(plugin)
//    private val removeCommand = TtCharacterContextPronounsRemoveCommand()
//    private val setChanceCommand = TtCharacterContextPronounsSetChanceCommand()

    private val addAliases = listOf("add")
    private val removeAliases = listOf("remove")
    private val setChanceAliases = listOf("setchance")

    private val subcommands = addAliases +
        removeAliases +
        setChanceAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in addAliases -> addCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
//            in removeAliases -> removeCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
//            in setChanceAliases -> setChanceCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /character context pronouns [${subcommands.joinToString("|")}]")
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
            in addAliases -> addCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in removeAliases -> removeCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in setChanceAliases -> setChanceCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
        else -> emptyList()
    }
}
