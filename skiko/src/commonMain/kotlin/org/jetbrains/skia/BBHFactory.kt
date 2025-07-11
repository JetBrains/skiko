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

@ExternalSymbolName("org_jetbrains_skia_RTreeFactory__1nMake")
@ModuleImport("org_jetbrains_skia_RTreeFactory__1nMake")
private external fun RTreeFactory_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BBHFactory__1nGetFinalizer")
@ModuleImport("org_jetbrains_skia_BBHFactory__1nGetFinalizer")
private external fun BBHFactory_nGetFinalizer(): NativePointer
