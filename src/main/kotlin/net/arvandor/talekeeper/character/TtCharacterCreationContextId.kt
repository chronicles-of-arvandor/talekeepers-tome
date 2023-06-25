package net.arvandor.talekeeper.character

import java.util.*

@JvmInline
value class TtCharacterCreationContextId(val value: String) {
    companion object {
        fun generate() = TtCharacterCreationContextId(UUID.randomUUID().toString())
    }
}
