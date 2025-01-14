package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

abstract class PictureFilterCanvas(canvas: Canvas) :
    Canvas(makePictureFilterCanvas(canvas), true, this) {
    private companion object {
        init {
            staticLoad()
        }
    }
    init {
        Stats.onNativeCall()
        try {
            doInit(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    protected abstract fun onDrawPicture(picture: Picture, matrix: Matrix33? = null, paint: Paint? = null): Boolean

    fun onDrawPicture(picturePtr: NativePointer, matrixPtr: NativePointer, paintPtr: NativePointer): Boolean {
        val picture = Picture(picturePtr, managed = false)
        // TODO: Provide mapping for matrix arg
        val paint = if (paintPtr == NullPointer) null else Paint(paintPtr, managed = false)
        return onDrawPicture(picture, null, paint)
    }
}

private fun makePictureFilterCanvas(canvas: Canvas): NativePointer {
    Stats.onNativeCall()
    return try {
        PictureFilterCanvas_nMake(getPtr(canvas))
    } finally {
        reachabilityBarrier(canvas)
    }
}

internal expect fun PictureFilterCanvas.doInit(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PictureFilterCanvas__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PictureFilterCanvas__1nMake")
private external fun PictureFilterCanvas_nMake(canvasPtr: NativePointer): NativePointer
