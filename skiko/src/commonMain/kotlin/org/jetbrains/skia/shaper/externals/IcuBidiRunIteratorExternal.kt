@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_IcuBidiRunIterator__1nMake")
internal external fun IcuBidiRunIterator_nMake(textPtr: NativePointer, bidiLevel: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_IcuBidiRunIterator__1nGetCurrentLevel")
internal external fun IcuBidiRunIterator_nGetCurrentLevel(ptr: NativePointer): Int
