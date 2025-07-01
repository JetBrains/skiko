package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArraySize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextBox__1nGetArraySize")
internal external fun TextBox_nGetArraySize(array: InteropPointer): Int
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nDisposeArray")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextBox__1nDisposeArray")
internal external fun TextBox_nDisposeArray(array: InteropPointer)
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArrayElement")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextBox__1nGetArrayElement")
internal external fun TextBox_nGetArrayElement(array: InteropPointer, index: Int, rectArray: InteropPointer, directionArray: InteropPointer)
