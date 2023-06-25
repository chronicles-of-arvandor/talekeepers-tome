package net.arvandor.talekeeper.spell

import java.util.*

@JvmInline
value class TtSpellId(val value: String) {
    companion object {
        fun generate() = TtSpellId(UUID.randomUUID().toString())
    }
}
