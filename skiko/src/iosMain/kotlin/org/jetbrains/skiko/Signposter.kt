package org.jetbrains.skiko

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.darwin.*

fun interface SignpostIntervalEndCallback {
    fun invoke()
}

class Signposter(
    subsystem: String,
    category: String,
) {
    val log = os_log_create(subsystem, category)
    val buffer = UByteArray(1024)

    inline fun emitEvent(name: String, id: os_signpost_id_t, type: os_signpost_type_t) {
        buffer.usePinned {
            _os_signpost_emit_with_name_impl(
                __dso_handle.ptr,
                log,
                type,
                id,
                name,
                "",
                it.addressOf(0),
                buffer.size.toUInt()
            )
        }
    }

    inline fun traceInterval(name: String, block: () -> Unit) {
        val id = os_signpost_id_generate(log)
        emitEvent(name, id, OS_SIGNPOST_INTERVAL_BEGIN)
        block()
        emitEvent(name, id, OS_SIGNPOST_INTERVAL_END)
    }

    fun startTrace(name: String): SignpostIntervalEndCallback {
        val id = os_signpost_id_generate(log)

        emitEvent(name, id, OS_SIGNPOST_INTERVAL_BEGIN)

        return SignpostIntervalEndCallback {
            emitEvent(name, id, OS_SIGNPOST_INTERVAL_END)
        }
    }
}