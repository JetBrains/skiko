package org.jetbrains.skija

import org.jetbrains.skija.impl.*
import java.lang.ref.Reference
import java.nio.ByteBuffer

class Pixmap internal constructor(ptr: Long, managed: Boolean) :
    Managed(ptr, _FinalizerHolder.PTR, managed) {
    constructor() : this(_nMakeNull(), true) {
        Stats.onNativeCall()
    }

    fun reset() {
        Stats.onNativeCall()
        _nReset(_ptr)
        Reference.reachabilityFence(this)
    }

    fun reset(info: ImageInfo, addr: Long, rowBytes: Int) {
        Stats.onNativeCall()
        _nResetWithInfo(
            _ptr,
            info.width, info.height,
            info.colorInfo.colorType.ordinal,
            info.colorInfo.alphaType.ordinal,
            Native.getPtr(info.colorInfo.colorSpace), addr, rowBytes
        )
        Reference.reachabilityFence(this)
        Reference.reachabilityFence(info.colorInfo.colorSpace)
    }

    fun reset(info: ImageInfo, buffer: ByteBuffer, rowBytes: Int) {
        reset(info, BufferUtil.getPointerFromByteBuffer(buffer), rowBytes)
    }

    fun setColorSpace(colorSpace: ColorSpace?) {
        Stats.onNativeCall()
        _nSetColorSpace(_ptr, Native.Companion.getPtr(colorSpace))
        Reference.reachabilityFence(this)
        Reference.reachabilityFence(colorSpace)
    }

    fun extractSubset(subsetPtr: Long, area: IRect): Boolean {
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
            Reference.reachabilityFence(this)
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
                Reference.reachabilityFence(this)
            }
        }
    val rowBytes: Int
        get() {
            Stats.onNativeCall()
            return try {
                _nGetRowBytes(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val addr: Long
        get() {
            Stats.onNativeCall()
            return try {
                _nGetAddr(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val rowBytesAsPixels: Int
        get() {
            Stats.onNativeCall()
            return try {
                _nGetRowBytesAsPixels(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    fun computeByteSize(): Int {
        Stats.onNativeCall()
        return try {
            _nComputeByteSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun computeIsOpaque(): Boolean {
        Stats.onNativeCall()
        return try {
            _nComputeIsOpaque(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getColor(x: Int, y: Int): Int {
        Stats.onNativeCall()
        return try {
            _nGetColor(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getAlphaF(x: Int, y: Int): Float {
        Stats.onNativeCall()
        return try {
            _nGetAlphaF(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getAddr(x: Int, y: Int): Long {
        Stats.onNativeCall()
        return try {
            _nGetAddrAt(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun readPixels(info: ImageInfo, addr: Long, rowBytes: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixels(
                _ptr,
                info.width, info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                Native.getPtr(info.colorInfo.colorSpace), addr, rowBytes
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(info.colorInfo.colorSpace)
        }
    }

    fun readPixels(info: ImageInfo, addr: Long, rowBytes: Int, srcX: Int, srcY: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsFromPoint(
                _ptr,
                info.width, info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                Native.getPtr(info.colorInfo.colorSpace), addr, rowBytes,
                srcX, srcY
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(info.colorInfo.colorSpace)
        }
    }

    fun readPixels(pixmap: Pixmap?): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsToPixmap(
                _ptr,
                Native.Companion.getPtr(pixmap)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
        }
    }

    fun readPixels(pixmap: Pixmap?, srcX: Int, srcY: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsToPixmapFromPoint(
                _ptr,
                Native.Companion.getPtr(pixmap),
                srcX,
                srcY
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
        }
    }

    fun scalePixels(dstPixmap: Pixmap?, samplingMode: SamplingMode): Boolean {
        Stats.onNativeCall()
        return try {
            _nScalePixels(
                _ptr,
                Native.Companion.getPtr(dstPixmap),
                samplingMode._pack()
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(dstPixmap)
        }
    }

    fun erase(color: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nErase(_ptr, color)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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

        fun make(info: ImageInfo, addr: Long, rowBytes: Int): Pixmap {
            return try {
                val ptr = _nMake(
                    info.width, info.height,
                    info.colorInfo.colorType.ordinal,
                    info.colorInfo.alphaType.ordinal,
                    Native.Companion.getPtr(info.colorInfo.colorSpace), addr, rowBytes
                )
                require(ptr != 0L) { "Failed to create Pixmap." }
                Pixmap(ptr, true)
            } finally {
                Reference.reachabilityFence(info.colorInfo.colorSpace)
            }
        }

        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMakeNull(): Long
        @JvmStatic external fun _nMake(
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            pixelsPtr: Long,
            rowBytes: Int
        ): Long

        @JvmStatic external fun _nReset(ptr: Long)
        @JvmStatic external fun _nResetWithInfo(
            ptr: Long,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            pixelsPtr: Long,
            rowBytes: Int
        )

        @JvmStatic external fun _nSetColorSpace(ptr: Long, colorSpacePtr: Long)
        @JvmStatic external fun _nExtractSubset(ptr: Long, subsetPtr: Long, l: Int, t: Int, r: Int, b: Int): Boolean
        @JvmStatic external fun _nGetInfo(ptr: Long): ImageInfo
        @JvmStatic external fun _nGetRowBytes(ptr: Long): Int
        @JvmStatic external fun _nGetAddr(ptr: Long): Long

        // TODO methods flattening ImageInfo not included yet - use GetInfo() instead.
        @JvmStatic external fun _nGetRowBytesAsPixels(ptr: Long): Int

        // TODO shiftPerPixel
        @JvmStatic external fun _nComputeByteSize(ptr: Long): Int
        @JvmStatic external fun _nComputeIsOpaque(ptr: Long): Boolean
        @JvmStatic external fun _nGetColor(ptr: Long, x: Int, y: Int): Int
        @JvmStatic external fun _nGetAlphaF(ptr: Long, x: Int, y: Int): Float
        @JvmStatic external fun _nGetAddrAt(ptr: Long, x: Int, y: Int): Long

        // methods related to C++ types(addr8/16/32/64, writable_addr8/16/32/64) not included - not needed
        @JvmStatic external fun _nReadPixels(
            ptr: Long,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            dstPixelsPtr: Long,
            dstRowBytes: Int
        ): Boolean

        @JvmStatic external fun _nReadPixelsFromPoint(
            ptr: Long,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            dstPixelsPtr: Long,
            dstRowBytes: Int,
            srcX: Int,
            srcY: Int
        ): Boolean

        @JvmStatic external fun _nReadPixelsToPixmap(ptr: Long, dstPixmapPtr: Long): Boolean
        @JvmStatic external fun _nReadPixelsToPixmapFromPoint(ptr: Long, dstPixmapPtr: Long, srcX: Int, srcY: Int): Boolean
        @JvmStatic external fun _nScalePixels(ptr: Long, dstPixmapPtr: Long, samplingOptions: Long): Boolean
        @JvmStatic external fun _nErase(ptr: Long, color: Int): Boolean
        @JvmStatic external fun _nEraseSubset(
            ptr: Long,
            color: Int,
            l: Int,
            t: Int,
            r: Int,
            b: Int
        ): Boolean // TODO float erase methods not included
    }
}