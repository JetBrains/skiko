package org.jetbrains.skiko.binding

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer

abstract class CHandled<T: CPointed>(val handle: CPointer<T>?) {

    init {
        if(handle == null) {
            throw NullPointerException("handle is null")
        }
    }
}