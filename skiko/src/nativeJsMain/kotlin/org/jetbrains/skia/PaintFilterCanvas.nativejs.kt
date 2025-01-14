package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope

internal actual fun PaintFilterCanvas.doInit(ptr: NativePointer) {
    interopScope {
        val onFilter = virtualBoolean {
            onFilter(PaintFilterCanvas_nGetOnFilterPaint(ptr))
        }
        PaintFilterCanvas_nInit(ptr, onFilter)
    }
}

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nInit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PaintFilterCanvas__1nInit")
internal external fun PaintFilterCanvas_nInit(ptr: NativePointer, onFilter: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_PaintFilterCanvas__1nGetOnFilterPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PaintFilterCanvas__1nGetOnFilterPaint")
internal external fun PaintFilterCanvas_nGetOnFilterPaint(ptr: NativePointer): NativePointer
