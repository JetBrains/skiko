@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFinalizer")
internal external fun Codec_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetImageInfo")
internal external fun Codec_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nReadPixels")
internal external fun Codec_nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, frame: Int, priorFrame: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nMakeFromData")
internal external fun Codec_nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetSizeWidth")
internal external fun Codec_nGetSizeWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetSizeHeight")
internal external fun Codec_nGetSizeHeight(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetEncodedOrigin")
internal external fun Codec_nGetEncodedOrigin(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetEncodedImageFormat")
internal external fun Codec_nGetEncodedImageFormat(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFrameCount")
internal external fun Codec_nGetFrameCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFrameInfo")
internal external fun Codec_nGetFrameInfo(ptr: NativePointer, frame: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFramesInfo")
internal external fun Codec_nGetFramesInfo(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetRepetitionCount")
internal external fun Codec_nGetRepetitionCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_Delete")
internal external fun FramesInfo_nDelete(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_GetSize")
internal external fun FramesInfo_nGetSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_GetInfos")
internal external fun FramesInfo_nGetInfos(ptr: NativePointer, result: InteropPointer)
