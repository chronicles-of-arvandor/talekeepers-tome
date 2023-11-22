package net.arvandor.talekeeper.mixpanel.event.player

import net.arvandor.talekeeper.mixpanel.TtMixpanelEvent
import java.util.*

data class TtMixpanelPlayerJoinedEvent(
    val minecraftUuid: UUID,
) : TtMixpanelEvent {
    override val distinctId = minecraftUuid.toString()
    override val eventName = "Player Joined"
    override val props: Map<String, Any?>? = null
}
