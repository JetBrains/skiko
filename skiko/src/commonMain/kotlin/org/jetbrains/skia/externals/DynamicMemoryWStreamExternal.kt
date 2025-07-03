@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nMake")
internal external fun DynamicMemoryWStream_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nGetFinalizer")
internal external fun DynamicMemoryWStream_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nBytesWritten")
internal external fun DynamicMemoryWStream_nBytesWritten(stream: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_DynamicMemoryWStream__1nRead")
internal external fun DynamicMemoryWStream_nRead(stream: NativePointer, buffer: InteropPointer, offset: Int, size: Int): Boolean
