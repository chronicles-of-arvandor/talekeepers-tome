package net.arvandor.talekeeper.mixpanel

import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.json.JSONObject
import java.util.*

class TtMixpanelService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val token = plugin.config.getString("mixpanel.token")
    private val mixpanel = MixpanelAPI()

    fun trackEvent(event: TtMixpanelEvent) {
        val messageBuilder = MessageBuilder(token)

        val props = event.props?.let { eventProps ->
            JSONObject().apply {
                eventProps.forEach { (key, value) ->
                    put(key, value)
                }
            }
        }

        val jsonEvent = messageBuilder.event(event.distinctId, event.eventName, props)
        mixpanel.sendMessage(jsonEvent)
    }

    fun updateUserProps(minecraftUuid: UUID, props: Map<String, Any>) {
        val messageBuilder = MessageBuilder(token)
        val jsonProps = JSONObject().apply {
            props.forEach { (key, value) ->
                put(key, JSONObject.wrap(value))
            }
        }
        val update = messageBuilder.set(minecraftUuid.toString(), jsonProps)

        mixpanel.sendMessage(update)
    }
}
