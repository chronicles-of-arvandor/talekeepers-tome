package net.arvandor.talekeeper.clazz

import java.util.*

@JvmInline
value class TtSubClassId(val value: String) {
    companion object {
        fun generate() = TtSubClassId(UUID.randomUUID().toString())
    }
}
