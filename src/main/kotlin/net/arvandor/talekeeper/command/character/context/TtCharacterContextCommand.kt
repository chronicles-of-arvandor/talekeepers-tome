package net.arvandor.talekeeper.command.character.context

import net.arvandor.talekeeper.TalekeepersTome
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtCharacterContextCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val nameCommand = TtCharacterContextNameCommand(plugin)
//    private val profileCommand = TtCharacterContextProfileCommand()
//    private val pronounsCommand = TtCharacterContextPronounsCommand()
//    private val ancestryCommand = TtCharacterContextAncestryCommand()
//    private val subAncestryCommand = TtCharacterContextSubAncestryCommand()
//    private val classCommand = TtCharacterContextClassCommand()
//    private val backgroundCommand = TtCharacterContextBackgroundCommand()
//    private val alignmentCommand = TtCharacterContextAlignmentCommand()
//    private val abilitiesCommand = TtCharacterContextAbilitiesCommand()
//    private val descriptionCommand = TtCharacterContextDescriptionCommand()
//    private val heightCommand = TtCharacterContextHeightCommand()
//    private val weightCommand = TtCharacterContextWeightCommand()
//    private val createCommand = TtCharacterContextCreateCommand()

    private val nameAliases = listOf("name")
    private val profileAliases = listOf("profile")
    private val pronounsAliases = listOf("pronouns")
    private val ancestryAliases = listOf("ancestry")
    private val subAncestryAliases = listOf("subancestry")
    private val classAliases = listOf("class")
    private val backgroundAliases = listOf("background")
    private val alignmentAliases = listOf("alignment")
    private val abilitiesAliases = listOf("abilities")
    private val descriptionAliases = listOf("description")
    private val heightAliases = listOf("height")
    private val weightAliases = listOf("weight")
    private val createAliases = listOf("create")
    private val subcommands = nameAliases +
        profileAliases +
        pronounsAliases +
        ancestryAliases +
        subAncestryAliases +
        classAliases +
        backgroundAliases +
        alignmentAliases +
        abilitiesAliases +
        descriptionAliases +
        heightAliases +
        weightAliases +
        createAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        when (args.firstOrNull()?.lowercase()) {
            in nameAliases -> nameCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage("${RED}Usage: /character context [${subcommands.joinToString("|")}]")
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
            in nameAliases -> nameCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in profileAliases -> profileCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in pronounsAliases -> pronounsCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in ancestryAliases -> ancestryCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in subAncestryAliases -> subAncestryCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in classAliases -> classCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in backgroundAliases -> backgroundCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in alignmentAliases -> alignmentCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in abilitiesAliases -> abilitiesCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in descriptionAliases -> descriptionCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in heightAliases -> heightCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in weightAliases -> weightCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in createAliases -> createCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
        else -> emptyList()
    }
}
