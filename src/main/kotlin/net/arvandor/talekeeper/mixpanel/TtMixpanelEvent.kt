package net.arvandor.talekeeper.mixpanel

interface TtMixpanelEvent {

    val distinctId: String?
    val eventName: String
    val props: Map<String, Any?>?
}
