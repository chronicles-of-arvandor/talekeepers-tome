package net.arvandor.talekeeper.birthday.gui

import net.arvandor.magistersmonths.datetime.MmCalendar
import net.arvandor.magistersmonths.datetime.MmMonth
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.InventoryGuiPage
import net.arvandor.talekeeper.gui.page
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.YELLOW
import org.bukkit.Material.PAPER
import java.time.Instant

class TtBirthdaySelectionGui(plugin: TalekeepersTome, calendar: MmCalendar, year: Int?, month: MmMonth?) : InventoryGui(plugin, "${month?.name ?: calendar.months.first().name} ${year ?: (calendar.toMmDateTime(Instant.now()).year - 21)}", renderPage(calendar, month, year), 54)

private fun renderPage(
    calendar: MmCalendar,
    month: MmMonth?,
    year: Int?,
): InventoryGuiPage = page {
    val currentTime = calendar.toMmDateTime(Instant.now())
    val month = month ?: calendar.months.first()
    val year = year ?: (currentTime.year - 21)
    (1..((month.endDay - month.startDay) + 1)).forEachIndexed { slot, day ->
        icon(slot) {
            item = item(PAPER, "${YELLOW}$day ${month.name} $year")
            onClick = { player, _ ->
                player.performCommand("character context age set $year ${if (month.name.contains(" ")) "\"${month.name}\"" else month.name} $day")
                false
            }
        }
    }

    icon(45) {
        item = item(PAPER, "$GREEN<< Previous year (${year - 1})")
        onClick = { player, _ ->
            page = renderPage(calendar, month, year - 1)
            title = "${month.name} ${year - 1}"
            true
        }
    }
    val previousMonth: MmMonth
    val previousMonthYear: Int
    if (calendar.months.indexOf(month) > 0) {
        previousMonth = calendar.months[calendar.months.indexOf(month) - 1]
        previousMonthYear = year
    } else {
        previousMonth = calendar.months.last()
        previousMonthYear = year - 1
    }
    icon(46) {
        item = item(PAPER, "$GREEN< Previous month (${previousMonth.name} $previousMonthYear)")
        onClick = { _, _ ->
            page = renderPage(calendar, previousMonth, previousMonthYear)
            title = "${previousMonth.name} $previousMonthYear"
            true
        }
    }
    val nextMonth: MmMonth
    val nextMonthYear: Int
    if (calendar.months.indexOf(month) < calendar.months.lastIndex) {
        nextMonth = calendar.months[calendar.months.indexOf(month) + 1]
        nextMonthYear = year
    } else {
        nextMonth = calendar.months.first()
        nextMonthYear = year + 1
    }
    icon(52) {
        item = item(PAPER, "${GREEN}Next month (${nextMonth.name} $nextMonthYear) >")
        onClick = { _, _ ->
            page = renderPage(calendar, nextMonth, nextMonthYear)
            title = "${nextMonth.name} $nextMonthYear"
            true
        }
    }
    icon(53) {
        item = item(PAPER, "${GREEN}Next year (${year + 1}) >>")
        onClick = { _, _ ->
            page = renderPage(calendar, month, year + 1)
            title = "${month.name} ${year + 1}"
            true
        }
    }
}
