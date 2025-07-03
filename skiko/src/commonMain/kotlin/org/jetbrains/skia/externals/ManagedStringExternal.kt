@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nGetFinalizer")
internal external fun ManagedString_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nMake")
internal external fun ManagedString_nMake(s: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringSize")
internal external fun ManagedString_nStringSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_ManagedString__nStringData")
internal external fun ManagedString_nStringData(ptr: NativePointer, result: InteropPointer, size: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nInsert")
internal external fun ManagedString_nInsert(ptr: NativePointer, offset: Int, s: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nAppend")
internal external fun ManagedString_nAppend(ptr: NativePointer, s: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemoveSuffix")
internal external fun ManagedString_nRemoveSuffix(ptr: NativePointer, from: Int)

@ExternalSymbolName("org_jetbrains_skia_ManagedString__1nRemove")
internal external fun ManagedString_nRemove(ptr: NativePointer, from: Int, length: Int)
