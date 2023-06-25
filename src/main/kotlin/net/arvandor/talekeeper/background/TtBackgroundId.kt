package net.arvandor.talekeeper.background

import java.util.*

@JvmInline
value class TtBackgroundId(val value: String) {
    companion object {
        fun generate() = TtBackgroundId(UUID.randomUUID().toString())
    }
}
