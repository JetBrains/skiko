package org.jetbrains.skia.impl

expect abstract class RefCnt : Managed {
    protected constructor(ptr: Long)
    protected constructor(ptr: Long, allowClose: Boolean)
}