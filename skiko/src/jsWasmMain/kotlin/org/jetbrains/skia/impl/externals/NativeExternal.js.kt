@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.impl

import org.jetbrains.skia.ExternalSymbolName

// Those functions are defined by Emscripten.
@ExternalSymbolName("malloc")
internal external fun Native_malloc(size: Int): NativePointer

@ExternalSymbolName("free")
internal external fun Native_free(ptr: NativePointer)