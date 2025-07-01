package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

// TODO class BBoxHierarchy internal constructor(ptr: NativePointer) : RefCnt(ptr)

@ExternalSymbolName("org_jetbrains_skia_RTreeFactory__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_RTreeFactory__1nMake")
internal external fun RTreeFactory_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BBHFactory__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BBHFactory__1nGetFinalizer")
internal external fun BBHFactory_nGetFinalizer(): NativePointer
