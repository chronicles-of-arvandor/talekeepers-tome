package net.arvandor.talekeeper.character

import java.util.*

@JvmInline
value class TtCharacterId(val value: String) {
    companion object {
        fun generate() = TtCharacterId(UUID.randomUUID().toString())
    }
}
