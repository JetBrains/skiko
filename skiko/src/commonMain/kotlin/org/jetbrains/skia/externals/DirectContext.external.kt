@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nFlush")
internal external fun DirectContext_nFlush(ptr: NativePointer, surfacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nFlushDefault")
internal external fun DirectContext_nFlushDefault(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeGL")
internal external fun DirectContext_nMakeGL(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeMetal")
internal external fun DirectContext_nMakeMetal(devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeDirect3D")
internal external fun DirectContext_nMakeDirect3D(adapterPtr: NativePointer, devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nSubmit")
internal external fun DirectContext_nSubmit(ptr: NativePointer, syncCpu: Boolean)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nFlushAndSubmit")
internal external fun DirectContext_nFlushAndSubmit(ptr: NativePointer, surfacePtr: NativePointer, syncCpu: Boolean)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nReset")
internal external fun DirectContext_nReset(ptr: NativePointer, flags: Int)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nAbandon")
internal external fun DirectContext_nAbandon(ptr: NativePointer, flags: Int)
