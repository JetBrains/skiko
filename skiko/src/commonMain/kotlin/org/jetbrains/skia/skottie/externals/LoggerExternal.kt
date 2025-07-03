@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

// Native/JS only

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nMake")
internal external fun Logger_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nInit")
internal external fun Logger_nInit(ptr: NativePointer, onLog: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogMessage")
internal external fun Logger_nGetLogMessage(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogJson")
internal external fun Logger_nGetLogJson(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogLevel")
internal external fun Logger_nGetLogLevel(ptr: NativePointer): Int
