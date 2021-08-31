package org.jetbrains.skija.impl

import java.lang.ref.Reference

abstract class Native(ptr: Long) {
    var _ptr: Long
    override fun toString(): String {
        return javaClass.simpleName + "(_ptr=0x" + java.lang.Long.toString(_ptr, 16) + ")"
    }

    override fun equals(other: Any?): Boolean {
        return try {
            if (this === other) return true
            if (null == other) return false
            if (!javaClass.isInstance(other)) return false
            val nOther = other as Native
            if (_ptr == nOther._ptr) true else _nativeEquals(nOther)
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    open fun _nativeEquals(other: Native?): Boolean {
        return false
    }

    // FIXME two different pointers might point to equal objects
    override fun hashCode(): Int {
        return java.lang.Long.hashCode(_ptr)
    }

    companion object {
        fun getPtr(n: Native?): Long {
            return n?._ptr ?: 0
        }
    }

    init {
        if (ptr == 0L) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}