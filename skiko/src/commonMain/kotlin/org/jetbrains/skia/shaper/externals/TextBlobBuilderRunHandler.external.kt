@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nGetFinalizer")
internal external fun TextBlobBuilderRunHandler_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMake")
internal external fun TextBlobBuilderRunHandler_nMake(textPtr: NativePointer, offsetX: Float, offsetY: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMakeBlob")
internal external fun TextBlobBuilderRunHandler_nMakeBlob(ptr: NativePointer): NativePointer
