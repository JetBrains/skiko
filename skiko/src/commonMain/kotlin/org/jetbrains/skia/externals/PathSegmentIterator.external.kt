@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nNext")
internal external fun PathSegmentIterator_nNext(ptr: NativePointer, points: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer")
internal external fun PathSegmentIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nMake")
internal external fun PathSegmentIterator_nMake(pathPtr: NativePointer, forceClose: Boolean): NativePointer
