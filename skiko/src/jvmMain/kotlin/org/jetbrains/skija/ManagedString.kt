package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class ManagedString internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nMake(s: String?): Long
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nToString(ptr: Long): String
        @JvmStatic external fun _nInsert(ptr: Long, offset: Int, s: String?)
        @JvmStatic external fun _nAppend(ptr: Long, s: String?)
        @JvmStatic external fun _nRemoveSuffix(ptr: Long, from: Int)
        @JvmStatic external fun _nRemove(ptr: Long, from: Int, length: Int)

        init {
            staticLoad()
        }
    }

    constructor(s: String?) : this(_nMake(s)) {
        Stats.onNativeCall()
    }

    override fun toString(): String {
        return try {
            Stats.onNativeCall()
            _nToString(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun insert(offset: Int, s: String): ManagedString {
        Stats.onNativeCall()
        _nInsert(_ptr, offset, s)
        return this
    }

    fun append(s: String): ManagedString {
        Stats.onNativeCall()
        _nAppend(_ptr, s)
        return this
    }

    fun remove(from: Int): ManagedString {
        Stats.onNativeCall()
        _nRemoveSuffix(_ptr, from)
        return this
    }

    fun remove(from: Int, length: Int): ManagedString {
        Stats.onNativeCall()
        _nRemove(_ptr, from, length)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}