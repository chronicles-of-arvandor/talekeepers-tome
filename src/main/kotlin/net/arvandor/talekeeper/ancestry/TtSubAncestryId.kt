package net.arvandor.talekeeper.ancestry

import java.util.*

@JvmInline
value class TtSubAncestryId(val value: String) {
    companion object {
        fun generate() = TtSubAncestryId(UUID.randomUUID().toString())
    }
}
