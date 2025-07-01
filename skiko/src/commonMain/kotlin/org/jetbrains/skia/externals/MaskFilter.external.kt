package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeTable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_MaskFilter__1nMakeTable")
internal external fun MaskFilter_nMakeTable(table: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeBlur")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_MaskFilter__1nMakeBlur")
internal external fun MaskFilter_nMakeBlur(mode: Int, sigma: Float, respectCTM: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_MaskFilter__1nMakeShader")
internal external fun MaskFilter_nMakeShader(shaderPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeGamma")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_MaskFilter__1nMakeGamma")
internal external fun MaskFilter_nMakeGamma(gamma: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeClip")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_MaskFilter__1nMakeClip")
internal external fun MaskFilter_nMakeClip(min: Byte, max: Byte): NativePointer
