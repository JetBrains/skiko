package org.jetbrains.skia

import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats

// TODO class BBoxHierarchy internal constructor(ptr: NativePointer) : RefCnt(ptr)

abstract class BBHFactory internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    private object _FinalizerHolder {
        val PTR = BBHFactory_nGetFinalizer()
    }
}

class RTreeFactory : BBHFactory {
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor() : super(RTreeFactory_nMake()) {
        Stats.onNativeCall()
    }
}