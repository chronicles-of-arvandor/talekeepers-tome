package net.arvandor.talekeeper.pronouns

import java.util.*

@JvmInline
value class TtPronounSetId(val value: String) {
    companion object {
        fun generate() = TtPronounSetId(UUID.randomUUID().toString())
    }
}
