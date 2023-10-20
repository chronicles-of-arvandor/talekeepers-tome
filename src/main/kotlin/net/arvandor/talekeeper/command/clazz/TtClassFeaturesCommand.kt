package net.arvandor.talekeeper.command.clazz

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.args.unquote
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.clazz.TtClass
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.clazz.TtSubClassId
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.YELLOW
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtClassFeaturesCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    companion object {
        const val LINES_PER_FEATURE = 2
        const val LINES_PER_PAGE = 4
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${ChatColor.RED}No Minecraft profile service was found. Please contact an admin.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${ChatColor.RED}No character service was found. Please contact an admin.")
            return true
        }

        val classService = Services.INSTANCE[TtClassService::class.java]
        if (classService == null) {
            sender.sendMessage("${ChatColor.RED}No class service was found. Please contact an admin.")
            return true
        }

        val unquotedArgs = args.unquote()

        if (unquotedArgs.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Usage: /class features [class] (subclass|none)")
            return true
        }

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        if (choiceService == null) {
            sender.sendMessage("${ChatColor.RED}No choice service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val clazz = classService.getClass(TtClassId(unquotedArgs[0])) ?: classService.getClass(unquotedArgs[0])
            if (clazz == null) {
                sender.sendMessage("${ChatColor.RED}There is no class by that name.")
                return@asyncTask
            }

            val subClass = if (unquotedArgs.size < 2 || unquotedArgs[1] == "none") {
                null
            } else {
                clazz.getSubClass(TtSubClassId(unquotedArgs[1])) ?: clazz.getSubClass(unquotedArgs[1])
            }

            val features = (clazz.features.entries + (subClass?.features?.entries ?: emptyList()))
                .sortedBy { (level, _) -> level }
                .groupBy { (level, _) -> level }
                .mapValues { (_, features) -> features.flatMap { (_, feature) -> feature } }
            if (features.isEmpty()) {
                sender.sendMessage("${ChatColor.GRAY}There are no features for that class.")
                return@asyncTask
            }

            val page = if (unquotedArgs.size > 2) {
                unquotedArgs[2].toIntOrNull() ?: 1
            } else {
                1
            }

            val view = PaginatedView.fromStrings(
                "$YELLOW${clazz.name}${subClass?.let { " ${ChatColor.GRAY}(${it.name})" } ?: ""} features",
                features.flatMap { (level, features) ->
                    listOf(
                        "${YELLOW}Lv$level",
                        *features.flatMap { feature ->
                            listOf(
                                "${ChatColor.WHITE}${feature.name}",
                                "${ChatColor.GRAY}${feature.description}",
                            )
                        }.toTypedArray(),
                    )
                },
                "${ChatColor.GREEN}< Previous",
                "Click here to view the previous page",
                "${ChatColor.GREEN}Next >",
                "Click here to view the next page",
                { pageNumber -> "Page $pageNumber" },
                LINES_PER_PAGE,
                { pageNumber -> "/class features ${clazz.id.value} ${subClass?.id?.value ?: "none"} $pageNumber" },
            )

            if (view.isPageValid(page)) {
                view.sendPage(sender, page)
            } else {
                sender.sendMessage("${ChatColor.RED}Invalid page number.")
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val classService = Services.INSTANCE[TtClassService::class.java]
        val unquotedArgs = args.unquote()
        return when {
            unquotedArgs.isEmpty() -> classService.getAll().map(TtClass::name)
            unquotedArgs.size == 1 -> classService.getAll().map(TtClass::name)
                .filter { it.startsWith(unquotedArgs[0], ignoreCase = true) }
                .map { if (it.contains(" ")) "\"$it\"" else it }
            unquotedArgs.size == 2 -> (classService.getClass(TtClassId(unquotedArgs[0])) ?: classService.getClass(unquotedArgs[0]))
                ?.subClasses
                ?.map { it.name }
                ?.filter { it.startsWith(unquotedArgs[1], ignoreCase = true) }
                ?.map { if (it.contains(" ")) "\"$it\"" else it }
                ?: emptyList()
            unquotedArgs.size == 3 -> {
                val clazz = classService.getClass(TtClassId(unquotedArgs[0])) ?: classService.getClass(unquotedArgs[0]) ?: return emptyList()
                val subClass = if (unquotedArgs[1] == "none") {
                    null
                } else {
                    clazz.getSubClass(TtSubClassId(unquotedArgs[1])) ?: clazz.getSubClass(unquotedArgs[1])
                }
                val features = (clazz.features.entries + (subClass?.features?.entries ?: emptyList()))
                    .sortedBy { (level, _) -> level }
                    .groupBy { (level, _) -> level }
                    .mapValues { (_, features) -> features.flatMap { (_, feature) -> feature } }
                // Determine amount of pages: 1 line per level, 2 lines per feature, 4 lines per page.
                (1..features.entries.sumOf { (_, features) -> 1 + features.size * LINES_PER_FEATURE } / LINES_PER_PAGE)
                    .map(Int::toString)
                    .filter { it.startsWith(unquotedArgs[2], ignoreCase = true) }
            }
            else -> emptyList()
        }
    }
}
