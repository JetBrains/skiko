@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nMake")
internal external fun HbIcuScriptRunIterator_nMake(textPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nGetCurrentScriptTag")
internal external fun HbIcuScriptRunIterator_nGetCurrentScriptTag(ptr: NativePointer): Int
