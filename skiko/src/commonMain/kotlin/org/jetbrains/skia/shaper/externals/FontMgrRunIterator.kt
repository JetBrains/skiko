@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_FontMgrRunIterator__1nMake")
internal external fun FontMgrRunIterator_nMake(textPtr: NativePointer, fontPtr: NativePointer, fontMgrPtr: NativePointer, optsBooleanProps: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_FontMgrRunIterator__1nGetCurrentFont")
internal external fun FontMgrRunIterator_nGetCurrentFont(ptr: NativePointer): NativePointer
