@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMakeEmpty")
internal external fun FontStyleSet_nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nCount")
internal external fun FontStyleSet_nCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyle")
internal external fun FontStyleSet_nGetStyle(ptr: NativePointer, index: Int): Int

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyleName")
internal external fun FontStyleSet_nGetStyleName(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetTypeface")
internal external fun FontStyleSet_nGetTypeface(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMatchStyle")
internal external fun FontStyleSet_nMatchStyle(ptr: NativePointer, style: Int): NativePointer
