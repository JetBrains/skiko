package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nGetArraySize")
internal external fun StdVectorDecoder_nGetArraySize(array: NativePointer): Int
@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nDisposeArray")
internal external fun StdVectorDecoder_nDisposeArray(array: NativePointer)
@ExternalSymbolName("org_jetbrains_skia_StdVectorDecoder__1nReleaseElement")
internal external fun StdVectorDecoder_nReleaseElement(array: NativePointer, index: Int): NativePointer
