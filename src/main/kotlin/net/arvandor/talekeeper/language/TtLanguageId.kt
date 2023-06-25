package net.arvandor.talekeeper.language

import java.util.*

@JvmInline
value class TtLanguageId(val value: String) {
    companion object {
        fun generate() = TtLanguageId(UUID.randomUUID().toString())
    }
}
