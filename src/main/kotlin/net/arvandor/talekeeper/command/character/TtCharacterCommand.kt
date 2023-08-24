package net.arvandor.talekeeper.command.character

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.command.character.alignment.TtCharacterAlignmentCommand
import net.arvandor.talekeeper.command.character.ancestry.TtCharacterAncestryCommand
import net.arvandor.talekeeper.command.character.card.TtCharacterCardCommand
import net.arvandor.talekeeper.command.character.context.TtCharacterContextCommand
import net.arvandor.talekeeper.command.character.create.TtCharacterCreateCommand
import net.arvandor.talekeeper.command.character.description.TtCharacterDescriptionCommand
import net.arvandor.talekeeper.command.character.height.TtCharacterHeightCommand
import net.arvandor.talekeeper.command.character.levelup.TtCharacterLevelUpCommand
import net.arvandor.talekeeper.command.character.name.TtCharacterNameCommand
import net.arvandor.talekeeper.command.character.profile.TtCharacterProfileCommand
import net.arvandor.talekeeper.command.character.pronouns.TtCharacterPronounsCommand
import net.arvandor.talekeeper.command.character.requests.TtCharacterRequestsCommand
import net.arvandor.talekeeper.command.character.subclass.TtCharacterSubClassCommand
import net.arvandor.talekeeper.command.character.weight.TtCharacterWeightCommand
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtCharacterCommand(plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val contextCommand = TtCharacterContextCommand(plugin)
    private val cardCommand = TtCharacterCardCommand(plugin)
    private val createCommand = TtCharacterCreateCommand(plugin)
    private val nameCommand = TtCharacterNameCommand(plugin)
    private val profileCommand = TtCharacterProfileCommand(plugin)
    private val pronounsCommand = TtCharacterPronounsCommand(plugin)
    private val ancestryCommand = TtCharacterAncestryCommand(plugin)
    private val alignmentCommand = TtCharacterAlignmentCommand(plugin)
    private val descriptionCommand = TtCharacterDescriptionCommand(plugin)
    private val heightCommand = TtCharacterHeightCommand(plugin)
    private val weightCommand = TtCharacterWeightCommand(plugin)
    private val subclassCommand = TtCharacterSubClassCommand(plugin)
    private val levelUpCommand = TtCharacterLevelUpCommand(plugin)
    private val requestsCommand = TtCharacterRequestsCommand(plugin)

    private val contextAliases = listOf("context", "ctx")
    private val cardAliases = listOf("card", "view", "show")
    private val createAliases = listOf("create", "new")
    private val nameAliases = listOf("name")
    private val profileAliases = listOf("profile")
    private val pronounsAliases = listOf("pronouns")
    private val ancestryAliases = listOf("ancestry", "race", "species")
    private val alignmentAliases = listOf("alignment")
    private val descriptionAliases = listOf("description")
    private val heightAliases = listOf("height")
    private val weightAliases = listOf("weight")
    private val subclassAliases = listOf("subclass")
    private val levelUpAliases = listOf("levelup")
    private val requestsAliases = listOf("requests", "request", "req", "reqs")

    private val subcommands = contextAliases +
        cardAliases +
        createAliases +
        nameAliases +
        profileAliases +
        pronounsAliases +
        ancestryAliases +
        alignmentAliases +
        descriptionAliases +
        heightAliases +
        weightAliases +
        subclassAliases +
        levelUpAliases +
        requestsAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return when (args.firstOrNull()?.lowercase()) {
            in contextAliases -> contextCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in cardAliases -> cardCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in createAliases -> createCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in nameAliases -> nameCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in profileAliases -> profileCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in pronounsAliases -> pronounsCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in ancestryAliases -> ancestryCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in alignmentAliases -> alignmentCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in descriptionAliases -> descriptionCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in heightAliases -> heightCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in weightAliases -> weightCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in subclassAliases -> subclassCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in levelUpAliases -> levelUpCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in requestsAliases -> requestsCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
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
            args.size > 1 -> when (args.first().lowercase()) {
                in contextAliases -> contextCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in cardAliases -> cardCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in createAliases -> createCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in nameAliases -> nameCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in profileAliases -> profileCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in pronounsAliases -> pronounsCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in ancestryAliases -> ancestryCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in alignmentAliases -> alignmentCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in descriptionAliases -> descriptionCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in heightAliases -> heightCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in weightAliases -> weightCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in subclassAliases -> subclassCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in levelUpAliases -> levelUpCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                in requestsAliases -> requestsCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
