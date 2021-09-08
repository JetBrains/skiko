@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import kotlin.jvm.JvmStatic

class Pixmap internal constructor(ptr: NativePointer, managed: Boolean) :
    Managed(ptr, _FinalizerHolder.PTR, managed) {
    constructor() : this(_nMakeNull(), true) {
        Stats.onNativeCall()
    }

    fun reset() {
        Stats.onNativeCall()
        _nReset(_ptr)
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

    fun reset(info: ImageInfo, buffer: ByteBuffer, rowBytes: Int) {
        reset(info, BufferUtil.getPointerFromByteBuffer(buffer), rowBytes)
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
            _nExtractSubset(
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

    fun extractSubset(buffer: ByteBuffer, area: IRect): Boolean {
        return extractSubset(BufferUtil.getPointerFromByteBuffer(buffer), area)
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
                _nGetRowBytes(_ptr)
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
                _nGetRowBytesAsPixels(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    fun computeByteSize(): Int {
        Stats.onNativeCall()
        return try {
            _nComputeByteSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun computeIsOpaque(): Boolean {
        Stats.onNativeCall()
        return try {
            _nComputeIsOpaque(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getColor(x: Int, y: Int): Int {
        Stats.onNativeCall()
        return try {
            _nGetColor(_ptr, x, y)
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

    val buffer: ByteBuffer?
        get() = BufferUtil.getByteBufferFromPointer(addr, computeByteSize())

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    companion object {
        fun make(info: ImageInfo, buffer: ByteBuffer, rowBytes: Int): Pixmap {
            return make(info, BufferUtil.getPointerFromByteBuffer(buffer), rowBytes)
        }

        fun make(info: ImageInfo, addr: NativePointer, rowBytes: Int): Pixmap {
            return try {
                val ptr = _nMake(
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

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nMakeNull")
        external fun _nMakeNull(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nMake")
        external fun _nMake(
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: NativePointer,
            pixelsPtr: NativePointer,
            rowBytes: Int
        ): NativePointer

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReset")
        external fun _nReset(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nResetWithInfo")
        external fun _nResetWithInfo(
            ptr: NativePointer,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: NativePointer,
            pixelsPtr: NativePointer,
            rowBytes: Int
        )

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nSetColorSpace")
        external fun _nSetColorSpace(ptr: NativePointer, colorSpacePtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nExtractSubset")
        external fun _nExtractSubset(ptr: NativePointer, subsetPtr: NativePointer, l: Int, t: Int, r: Int, b: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetInfo")
        external fun _nGetInfo(ptr: NativePointer): ImageInfo
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetRowBytes")
        external fun _nGetRowBytes(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAddr")
        external fun _nGetAddr(ptr: NativePointer): NativePointer

        // TODO methods flattening ImageInfo not included yet - use GetInfo() instead.
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetRowBytesAsPixels")
        external fun _nGetRowBytesAsPixels(ptr: NativePointer): Int

        // TODO shiftPerPixel
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nComputeByteSize")
        external fun _nComputeByteSize(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nComputeIsOpaque")
        external fun _nComputeIsOpaque(ptr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetColor")
        external fun _nGetColor(ptr: NativePointer, x: Int, y: Int): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAlphaF")
        external fun _nGetAlphaF(ptr: NativePointer, x: Int, y: Int): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nGetAddrAt")
        external fun _nGetAddrAt(ptr: NativePointer, x: Int, y: Int): NativePointer

        // methods related to C++ types(addr8/16/32/64, writable_addr8/16/32/64) not included - not needed
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixels")
        external fun _nReadPixels(
            ptr: NativePointer,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: NativePointer,
            dstPixelsPtr: NativePointer,
            dstRowBytes: Int
        ): Boolean

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsFromPoint")
        external fun _nReadPixelsFromPoint(
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

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsToPixmap")
        external fun _nReadPixelsToPixmap(ptr: NativePointer, dstPixmapPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nReadPixelsToPixmapFromPoint")
        external fun _nReadPixelsToPixmapFromPoint(ptr: NativePointer, dstPixmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nScalePixels")
        external fun _nScalePixels(ptr: NativePointer, dstPixmapPtr: NativePointer, samplingOptions: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nErase")
        external fun _nErase(ptr: NativePointer, color: Int): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Pixmap__1nEraseSubset")
        external fun _nEraseSubset(
            ptr: NativePointer,
            color: Int,
            l: Int,
            t: Int,
            r: Int,
            b: Int
        ): Boolean // TODO float erase methods not included
    }
}