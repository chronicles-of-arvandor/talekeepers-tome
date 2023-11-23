package net.arvandor.talekeeper.item

import com.rpkit.core.service.Service
import com.rpkit.core.service.Services
import net.arvandor.numinoustreasury.item.NuminousItemService
import net.arvandor.numinoustreasury.item.NuminousItemType
import net.arvandor.talekeeper.TalekeepersTome

class TtItemService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    fun getItemType(id: TtItemId): NuminousItemType? {
        val numinousItemService = Services.INSTANCE.get(NuminousItemService::class.java)
        return numinousItemService.getItemTypeById(id.value)
    }
}
