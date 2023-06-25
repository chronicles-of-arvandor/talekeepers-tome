package net.arvandor.talekeeper.feat

import java.util.*

@JvmInline
value class TtFeatId(val value: String) {
    companion object {
        fun generate() = TtFeatId(UUID.randomUUID().toString())
    }
}
