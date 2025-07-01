package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

@ExternalSymbolName("org_jetbrains_skia_svg_SVGCanvasKt__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_svg_SVGCanvasKt__1nMake")
internal external fun _nMake(left: Float, top: Float, right: Float, bottom: Float, wstreamPtr: NativePointer, flags: Int): NativePointer

