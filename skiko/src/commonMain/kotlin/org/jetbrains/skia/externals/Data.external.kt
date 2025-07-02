@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer


@ExternalSymbolName("org_jetbrains_skia_Data__1nGetFinalizer")
internal external fun Data_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nSize")
internal external fun Data_nSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Data__1nBytes")
internal external fun Data_nBytes(ptr: NativePointer, offset: Int, length: Int, destBytes: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Data__1nEquals")
internal external fun Data_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromBytes")
internal external fun Data_nMakeFromBytes(bytes: InteropPointer, offset: Int, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeWithoutCopy")
internal external fun Data_nMakeWithoutCopy(memoryAddr: NativePointer, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeFromFileName")
internal external fun Data_nMakeFromFileName(path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeSubset")
internal external fun Data_nMakeSubset(ptr: NativePointer, offset: Int, length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeEmpty")
internal external fun Data_nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nMakeUninitialized")
internal external fun Data_nMakeUninitialized(length: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Data__1nWritableData")
internal external fun Data_nWritableData(dataPtr: NativePointer): NativePointer
