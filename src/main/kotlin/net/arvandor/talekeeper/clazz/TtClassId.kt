package net.arvandor.talekeeper.clazz

import java.util.*

@JvmInline
value class TtClassId(val value: String) {
    companion object {
        fun generate() = TtClassId(UUID.randomUUID().toString())
    }
}
