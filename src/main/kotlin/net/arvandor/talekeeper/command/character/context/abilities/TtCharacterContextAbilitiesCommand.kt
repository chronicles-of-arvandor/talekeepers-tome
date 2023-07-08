package net.arvandor.talekeeper.command.character.context.abilities

import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtCharacterContextAbilitiesCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val setCommand = TtCharacterContextAbilitiesSetCommand(plugin)
    private val confirmCommand = TtCharacterContextAbilitiesConfirmCommand(plugin)

    private val setAliases = listOf("set")
    private val confirmAliases = listOf("confirm")

    private val subcommands = setAliases + confirmAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in setAliases -> setCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )

            in confirmAliases -> confirmCommand.onCommand(
                sender,
                command,
                label,
                args.drop(1).toTypedArray(),
            )
            else -> {
                sender.sendMessage("${RED}Usage: /character context abilities [${subcommands.joinToString("|")}]")
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
        else -> emptyList()
    }
}
