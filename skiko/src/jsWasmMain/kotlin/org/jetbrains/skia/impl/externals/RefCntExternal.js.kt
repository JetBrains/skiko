@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.impl

import org.jetbrains.skia.ExternalSymbolName

@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getFinalizer")
internal actual external fun RefCnt_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getRefCount")
internal external fun RefCnt_nGetRefCount(ptr: NativePointer): Int
