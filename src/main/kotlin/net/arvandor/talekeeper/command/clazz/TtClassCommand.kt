package net.arvandor.talekeeper.command.clazz

import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtClassCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val featuresCommand = TtClassFeaturesCommand(plugin)
    private val whatsNewCommand = TtClassWhatsNewCommand(plugin)

    private val featuresAliases = listOf("features")
    private val whatsNewAliases = listOf("whatsnew")

    private val subcommands = featuresAliases + whatsNewAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in featuresAliases -> featuresCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in whatsNewAliases -> whatsNewCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /class [${subcommands.joinToString("|")}]")
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
            in featuresAliases -> featuresCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in whatsNewAliases -> whatsNewCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
        else -> emptyList()
    }
}
