package net.arvandor.talekeeper.choice

import java.util.*

@JvmInline
value class TtChoiceId(val value: String) {
    companion object {
        fun generate() = TtChoiceId(UUID.randomUUID().toString())
    }
}
