@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.impl

import org.jetbrains.skia.ExternalSymbolName

@ExternalSymbolName("org_jetbrains_skia_impl_Managed__invokeFinalizer")
internal external fun Managed_nInvokeFinalizer(finalizer: NativePointer, obj: NativePointer)
