package org.jetbrains.skia.svg

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGNode__1nGetTag")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGNode__1nGetTag")
internal external fun SVGNode_nGetTag(ptr: NativePointer): Int
