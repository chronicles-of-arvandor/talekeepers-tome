package net.arvandor.talekeeper.ancestry

import java.util.*

@JvmInline
value class TtAncestryId(val value: String) {
    companion object {
        fun generate() = TtAncestryId(UUID.randomUUID().toString())
    }
}
