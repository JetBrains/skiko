@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nGetFinalizer")
internal external fun ManagedRunIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nConsume")
internal external fun ManagedRunIterator_nConsume(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nGetEndOfCurrentRun")
internal external fun ManagedRunIterator_nGetEndOfCurrentRun(ptr: NativePointer, textPtr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_shaper_ManagedRunIterator__1nIsAtEnd")
internal external fun ManagedRunIterator_nIsAtEnd(ptr: NativePointer): Boolean
