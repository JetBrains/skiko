@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.sksg

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetFinalizer")
internal external fun InvalidationController_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nMake")
internal external fun InvalidationController_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nInvalidate")
internal external fun InvalidationController_nInvalidate(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetBounds")
internal external fun InvalidationController_nGetBounds(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nReset")
internal external fun InvalidationController_nReset(ptr: NativePointer)
