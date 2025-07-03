@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nGetFinalizer")
internal external fun ParagraphBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nMake")
internal external fun ParagraphBuilder_nMake(paragraphStylePtr: NativePointer, fontCollectionPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPushStyle")
internal external fun ParagraphBuilder_nPushStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nPopStyle")
internal external fun ParagraphBuilder_nPopStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddText")
internal external fun ParagraphBuilder_nAddText(ptr: NativePointer, text: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nAddPlaceholder")
internal external fun ParagraphBuilder_nAddPlaceholder(
    ptr: NativePointer,
    width: Float,
    height: Float,
    alignment: Int,
    baselineMode: Int,
    baseline: Float
)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphBuilder__1nBuild")
internal external fun ParagraphBuilder_nBuild(ptr: NativePointer): NativePointer
