@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nGetFinalizer")
internal external fun AnimationBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nMake")
internal external fun AnimationBuilder_nMake(flags: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nSetFontManager")
internal external fun AnimationBuilder_nSetFontManager(ptr: NativePointer, fontMgrPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nSetLogger")
internal external fun AnimationBuilder_nSetLogger(ptr: NativePointer, loggerPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromString")
internal external fun AnimationBuilder_nBuildFromString(ptr: NativePointer, data: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromFile")
internal external fun AnimationBuilder_nBuildFromFile(ptr: NativePointer, path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_AnimationBuilder__1nBuildFromData")
internal external fun AnimationBuilder_nBuildFromData(ptr: NativePointer, dataPtr: NativePointer): NativePointer
