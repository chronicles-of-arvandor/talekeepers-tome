package net.arvandor.talekeeper.mixpanel

import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.OfflinePlayer
import org.json.JSONObject

class TtMixpanelService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val token = plugin.config.getString("mixpanel.token")
    private val mixpanel = MixpanelAPI()

    fun trackEvent(event: TtMixpanelEvent) {
        val messageBuilder = MessageBuilder(token)

        val props = JSONObject()
        if (event.player?.isOnline == true) {
            val onlinePlayer = event.player?.player
            onlinePlayer?.address?.address?.hostAddress?.let { props.put("ip", it) }
        }
        event.props?.let { eventProps ->
            eventProps.forEach { (key, value) ->
                props.put(key, JSONObject.wrap(value))
            }
        }

        val jsonEvent = messageBuilder.event(event.player?.uniqueId?.toString(), event.eventName, props)
        mixpanel.sendMessage(jsonEvent)
    }

    fun updateUserProps(player: OfflinePlayer, props: Map<String, Any>) {
        val messageBuilder = MessageBuilder(token)
        val jsonProps = JSONObject().apply {
            if (player.isOnline) {
                val onlinePlayer = player.player
                onlinePlayer?.address?.address?.hostAddress?.let { put("ip", it) }
            }
            props.forEach { (key, value) ->
                put(key, JSONObject.wrap(value))
            }
        }
        val update = messageBuilder.set(player.uniqueId.toString(), jsonProps)

        mixpanel.sendMessage(update)
    }
}
