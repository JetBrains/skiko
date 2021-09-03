package org.jetbrains.skia.impl

actual abstract class RefCnt : Managed {
    actual protected constructor(ptr: Long): super(ptr, 0L, false) { TODO() }
    actual protected constructor(ptr: Long, allowClose: Boolean): super(ptr, 0, allowClose) { TODO() }
}