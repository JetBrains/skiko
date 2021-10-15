package org.jetbrains.skia

import org.jetbrains.skia.impl.*

class Pixmap internal constructor(ptr: NativePointer, managed: Boolean) :
    Managed(ptr, _FinalizerHolder.PTR, managed) {
    constructor() : this(_nMakeNull(), true) {
        Stats.onNativeCall()
    }

    fun reset() {
        Stats.onNativeCall()
        Pixmap_nReset(_ptr)
        reachabilityBarrier(this)
    }

    fun reset(info: ImageInfo, addr: NativePointer, rowBytes: Int) {
        Stats.onNativeCall()
        _nResetWithInfo(
            _ptr,
            info.width, info.height,
            info.colorInfo.colorType.ordinal,
            info.colorInfo.alphaType.ordinal,
            getPtr(info.colorInfo.colorSpace), addr, rowBytes
        )
        reachabilityBarrier(this)
        reachabilityBarrier(info.colorInfo.colorSpace)
    }

    fun reset(info: ImageInfo, buffer: SkikoByteBuffer, rowBytes: Int) {
        reset(info, buffer.addr, rowBytes)
    }

    fun setColorSpace(colorSpace: ColorSpace?) {
        Stats.onNativeCall()
        _nSetColorSpace(_ptr, getPtr(colorSpace))
        reachabilityBarrier(this)
        reachabilityBarrier(colorSpace)
    }

    fun extractSubset(subsetPtr: NativePointer, area: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            Pixmap_nExtractSubset(
                _ptr,
                subsetPtr,
                area.left,
                area.top,
                area.right,
                area.bottom
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun extractSubset(buffer: SkikoByteBuffer, area: IRect): Boolean {
        return extractSubset(buffer.addr, area)
    }

    val info: ImageInfo
        get() {
            Stats.onNativeCall()
            return try {
                _nGetInfo(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val rowBytes: Int
        get() {
            Stats.onNativeCall()
            return try {
                Pixmap_nGetRowBytes(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val addr: NativePointer
        get() {
            Stats.onNativeCall()
            return try {
                _nGetAddr(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val rowBytesAsPixels: Int
        get() {
            Stats.onNativeCall()
            return try {
                Pixmap_nGetRowBytesAsPixels(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    fun computeByteSize(): Int {
        Stats.onNativeCall()
        return try {
            Pixmap_nComputeByteSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun computeIsOpaque(): Boolean {
        Stats.onNativeCall()
        return try {
            Pixmap_nComputeIsOpaque(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getColor(x: Int, y: Int): Int {
        Stats.onNativeCall()
        return try {
            Pixmap_nGetColor(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getAlphaF(x: Int, y: Int): Float {
        Stats.onNativeCall()
        return try {
            _nGetAlphaF(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getAddr(x: Int, y: Int): NativePointer {
        Stats.onNativeCall()
        return try {
            _nGetAddrAt(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun readPixels(info: ImageInfo, addr: NativePointer, rowBytes: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixels(
                _ptr,
                info.width, info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                getPtr(info.colorInfo.colorSpace), addr, rowBytes
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(info.colorInfo.colorSpace)
        }
    }

    fun readPixels(info: ImageInfo, addr: NativePointer, rowBytes: Int, srcX: Int, srcY: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsFromPoint(
                _ptr,
                info.width, info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                getPtr(info.colorInfo.colorSpace), addr, rowBytes,
                srcX, srcY
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(info.colorInfo.colorSpace)
        }
    }

    fun readPixels(pixmap: Pixmap?): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsToPixmap(
                _ptr,
                getPtr(pixmap)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    fun readPixels(pixmap: Pixmap?, srcX: Int, srcY: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsToPixmapFromPoint(
                _ptr,
                getPtr(pixmap),
                srcX,
                srcY
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    fun scalePixels(dstPixmap: Pixmap?, samplingMode: SamplingMode): Boolean {
        Stats.onNativeCall()
        return try {
            _nScalePixels(
                _ptr,
                getPtr(dstPixmap),
                samplingMode._pack()
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dstPixmap)
        }
    }

    fun erase(color: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nErase(_ptr, color)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun erase(color: Int, subset: IRect): Boolean {
        Stats.onNativeCall()
        return try {
            _nEraseSubset(
                _ptr,
                color,
                subset.left,
                subset.top,
                subset.right,
                subset.bottom
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    val buffer: SkikoByteBuffer?
        get() = SkikoByteBuffer(addr, computeByteSize())

    private object _FinalizerHolder {
        val PTR = Pixmap_nGetFinalizer()
    }

    companion object {
        fun make(info: ImageInfo, buffer: SkikoByteBuffer, rowBytes: Int): Pixmap {
            return make(info, buffer.addr, rowBytes)
        }

        fun make(info: ImageInfo, addr: NativePointer, rowBytes: Int): Pixmap {
            return try {
                val ptr = Pixmap_nMake(
                    info.width, info.height,
                    info.colorInfo.colorType.ordinal,
                    info.colorInfo.alphaType.ordinal,
                    getPtr(info.colorInfo.colorSpace), addr, rowBytes
                )
                require(ptr != NullPointer) { "Failed to create Pixmap." }
                Pixmap(ptr, true)
            } finally {
                reachabilityBarrier(info.colorInfo.colorSpace)
            }
        }
    }
}


@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetFinalizer")
private external fun Pixmap_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReset")
private external fun Pixmap_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nExtractSubset")
private external fun Pixmap_nExtractSubset(ptr: NativePointer, subsetPtr: NativePointer, l: Int, t: Int, r: Int, b: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetRowBytes")
private external fun Pixmap_nGetRowBytes(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetRowBytesAsPixels")
private external fun Pixmap_nGetRowBytesAsPixels(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nComputeByteSize")
private external fun Pixmap_nComputeByteSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nComputeIsOpaque")
private external fun Pixmap_nComputeIsOpaque(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetColor")
private external fun Pixmap_nGetColor(ptr: NativePointer, x: Int, y: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nMakeNull")
private external fun _nMakeNull(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nMake")
private external fun Pixmap_nMake(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixelsPtr: NativePointer,
    rowBytes: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nResetWithInfo")
private external fun _nResetWithInfo(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixelsPtr: NativePointer,
    rowBytes: Int
)


@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nSetColorSpace")
private external fun _nSetColorSpace(ptr: NativePointer, colorSpacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetInfo")
private external fun _nGetInfo(ptr: NativePointer): ImageInfo

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAddr")
private external fun _nGetAddr(ptr: NativePointer): NativePointer

// TODO methods flattening ImageInfo not included yet - use GetInfo() instead.

// TODO shiftPerPixel

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAlphaF")
private external fun _nGetAlphaF(ptr: NativePointer, x: Int, y: Int): Float

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAddrAt")
private external fun _nGetAddrAt(ptr: NativePointer, x: Int, y: Int): NativePointer

// methods related to C++ types(addr8/16/32/64, writable_addr8/16/32/64) not included - not needed

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixels")
private external fun _nReadPixels(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dstPixelsPtr: NativePointer,
    dstRowBytes: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsFromPoint")
private external fun _nReadPixelsFromPoint(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dstPixelsPtr: NativePointer,
    dstRowBytes: Int,
    srcX: Int,
    srcY: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsToPixmap")
private external fun _nReadPixelsToPixmap(ptr: NativePointer, dstPixmapPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsToPixmapFromPoint")
private external fun _nReadPixelsToPixmapFromPoint(ptr: NativePointer, dstPixmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nScalePixels")
private external fun _nScalePixels(ptr: NativePointer, dstPixmapPtr: NativePointer, samplingOptions: Long): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nErase")
private external fun _nErase(ptr: NativePointer, color: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Pixmap__1nEraseSubset")
private external fun _nEraseSubset(
    ptr: NativePointer,
    color: Int,
    l: Int,
    t: Int,
    r: Int,
    b: Int
): Boolean // TODO float erase methods not included
