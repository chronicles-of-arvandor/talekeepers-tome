package net.arvandor.talekeeper.command.character.context

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.command.character.context.abilities.TtCharacterContextAbilitiesCommand
import net.arvandor.talekeeper.command.character.context.alignment.TtCharacterContextAlignmentCommand
import net.arvandor.talekeeper.command.character.context.ancestry.TtCharacterContextAncestryCommand
import net.arvandor.talekeeper.command.character.context.background.TtCharacterContextBackgroundCommand
import net.arvandor.talekeeper.command.character.context.clazz.TtCharacterContextClassCommand
import net.arvandor.talekeeper.command.character.context.name.TtCharacterContextNameCommand
import net.arvandor.talekeeper.command.character.context.profile.TtCharacterContextProfileCommand
import net.arvandor.talekeeper.command.character.context.pronouns.TtCharacterContextPronounsCommand
import net.arvandor.talekeeper.command.character.context.subancestry.TtCharacterContextSubAncestryCommand
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level

class TtCharacterContextCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val nameCommand = TtCharacterContextNameCommand(plugin)
    private val profileCommand = TtCharacterContextProfileCommand(plugin)
    private val pronounsCommand = TtCharacterContextPronounsCommand(plugin)
    private val ancestryCommand = TtCharacterContextAncestryCommand(plugin)
    private val subAncestryCommand = TtCharacterContextSubAncestryCommand(plugin)
    private val classCommand = TtCharacterContextClassCommand(plugin)
    private val backgroundCommand = TtCharacterContextBackgroundCommand(plugin)
    private val alignmentCommand = TtCharacterContextAlignmentCommand(plugin)
    private val abilitiesCommand = TtCharacterContextAbilitiesCommand(plugin)
//    private val descriptionCommand = TtCharacterContextDescriptionCommand()
//    private val heightCommand = TtCharacterContextHeightCommand()
//    private val weightCommand = TtCharacterContextWeightCommand()
//    private val createCommand = TtCharacterContextCreateCommand()

    private val nameAliases = listOf("name")
    private val profileAliases = listOf("profile")
    private val pronounsAliases = listOf("pronouns")
    private val ancestryAliases = listOf("ancestry", "race", "species")
    private val subAncestryAliases = listOf("subancestry", "subrace", "subspecies")
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
            in profileAliases -> profileCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in pronounsAliases -> pronounsCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in ancestryAliases -> ancestryCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in subAncestryAliases -> subAncestryCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in classAliases -> classCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in backgroundAliases -> backgroundCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in alignmentAliases -> alignmentCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in abilitiesAliases -> abilitiesCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
//            in descriptionAliases -> descriptionCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
//            in heightAliases -> heightCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
//            in weightAliases -> weightCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
//            in createAliases -> createCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                displayContext(sender)
                true
            }
        }

    private fun displayContext(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return
        }
        asyncTask(plugin) {
            val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                sender.sendMessage("${RED}No Minecraft profile service was found. Please contact an admin.")
                return@asyncTask
            }
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)

            val characterService = Services.INSTANCE[TtCharacterService::class.java]
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}Failed to retrieve character creation context. Please contact an admin.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            ctx.display(sender)
        }
        return
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
            in nameAliases -> nameCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in profileAliases -> profileCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in pronounsAliases -> pronounsCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in ancestryAliases -> ancestryCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in subAncestryAliases -> subAncestryCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in classAliases -> classCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in backgroundAliases -> backgroundCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in alignmentAliases -> alignmentCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            in abilitiesAliases -> abilitiesCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in descriptionAliases -> descriptionCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in heightAliases -> heightCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in weightAliases -> weightCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
//            in createAliases -> createCommand.onTabComplete(sender, command, label, args.drop(1).toTypedArray())
            else -> emptyList()
        }
        else -> emptyList()
    }
}
