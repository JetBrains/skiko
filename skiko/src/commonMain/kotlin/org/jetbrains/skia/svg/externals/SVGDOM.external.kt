package org.jetbrains.skia.svg

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nMakeFromData")
internal external fun SVGDOM_nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetRoot")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nGetRoot")
internal external fun SVGDOM_nGetRoot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nGetContainerSize")
internal external fun SVGDOM_nGetContainerSize(ptr: NativePointer, dst: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nSetContainerSize")
internal external fun SVGDOM_nSetContainerSize(ptr: NativePointer, width: Float, height: Float)

@ExternalSymbolName("org_jetbrains_skia_svg_SVGDOM__1nRender")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGDOM__1nRender")
internal external fun SVGDOM_nRender(ptr: NativePointer, canvasPtr: NativePointer)
