@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nGetFamiliesCount")
internal external fun FontMgr_nGetFamiliesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nGetFamilyName")
internal external fun FontMgr_nGetFamilyName(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMakeStyleSet")
internal external fun FontMgr_nMakeStyleSet(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMatchFamily")
internal external fun FontMgr_nMatchFamily(ptr: NativePointer, familyName: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMatchFamilyStyle")
internal external fun FontMgr_nMatchFamilyStyle(ptr: NativePointer, familyName: InteropPointer, fontStyle: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter")
internal external fun FontMgr_nMatchFamilyStyleCharacter(
    ptr: NativePointer,
    familyName: InteropPointer,
    fontStyle: Int,
    bcp47: InteropPointer,
    bcp47size: Int,
    character: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMakeFromData")
internal external fun FontMgr_nMakeFromData(ptr: NativePointer, dataPtr: NativePointer, ttcIndex: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMakeFromFile")
internal external fun FontMgr_nMakeFromFile(ptr: NativePointer, pathPtr: InteropPointer, ttcIndex: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nDefault")
internal external fun FontMgr_nDefault(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nLegacyMakeTypeface")
internal external fun FontMgr_nLegacyMakeTypeface(ptr: NativePointer, familyName: InteropPointer, fontStyle: Int): NativePointer
