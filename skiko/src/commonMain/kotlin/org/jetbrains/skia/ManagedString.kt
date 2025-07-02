package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult

class ManagedString internal constructor(ptr: NativePointer, managed: Boolean = true) : Managed(ptr, _FinalizerHolder.PTR, managed) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(s: String?) : this(
        interopScope {  ManagedString_nMake(toInterop(s)) }
    ) {
        Stats.onNativeCall()
    }

    override fun toString(): String {
        return try {
            Stats.onNativeCall()
            val size = ManagedString_nStringSize(_ptr)
            withResult(ByteArray(size)) {
                ManagedString_nStringData(_ptr, it, size)
            }.decodeToString()
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun insert(offset: Int, s: String): ManagedString {
        Stats.onNativeCall()
        interopScope {
            ManagedString_nInsert(_ptr, offset, toInterop(s))
        }
        return this
    }

    fun append(s: String): ManagedString {
        Stats.onNativeCall()
        interopScope {
            ManagedString_nAppend(_ptr, toInterop(s))
        }
        return this
    }

    fun remove(from: Int): ManagedString {
        Stats.onNativeCall()
        ManagedString_nRemoveSuffix(_ptr, from)
        return this
    }

    fun remove(from: Int, length: Int): ManagedString {
        Stats.onNativeCall()
        ManagedString_nRemove(_ptr, from, length)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = ManagedString_nGetFinalizer()
    }
}