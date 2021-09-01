package org.jetbrains.skija

import org.jetbrains.skija.impl.Managed

abstract class WStream : Managed {
    constructor(ptr: Long, finalizer: Long) : super(ptr, finalizer)
    constructor(ptr: Long, finalizer: Long, managed: Boolean) : super(ptr, finalizer, managed)
}