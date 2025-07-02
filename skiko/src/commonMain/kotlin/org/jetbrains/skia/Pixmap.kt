package org.jetbrains.skia

import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.maybeSynchronized

class Pixmap internal constructor(ptr: NativePointer, managed: Boolean) :
    Managed(ptr, _FinalizerHolder.PTR, managed) {

    /**
     * A reference to the underlying memory to prevent it from being cleaned up by GC.
     *  It's used in [reset] and [make].
     */
    private var underlyingMemoryOwner: Managed? = null

    private var _imageInfo: ImageInfo ? = null
    private val _imageInfoLock = Unit

    constructor() : this(Pixmap_nMakeNull(), true) {
        Stats.onNativeCall()
    }

    fun reset() {
        Stats.onNativeCall()
        maybeSynchronized(_imageInfoLock) {
            _imageInfo = null
            Pixmap_nReset(_ptr)
        }
        underlyingMemoryOwner = null
        reachabilityBarrier(this)
    }

    fun reset(info: ImageInfo, addr: NativePointer, rowBytes: Int, underlyingMemoryOwner: Managed? = null) {
        Stats.onNativeCall()
        maybeSynchronized(_imageInfoLock) {
            _imageInfo = null
            Pixmap_nResetWithInfo(
                _ptr,
                info.width, info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                getPtr(info.colorInfo.colorSpace), addr, rowBytes
            )
        }
        this.underlyingMemoryOwner = underlyingMemoryOwner
        reachabilityBarrier(this)
        reachabilityBarrier(info.colorInfo.colorSpace)
    }

    fun reset(info: ImageInfo, buffer: Data, rowBytes: Int) {
        reset(info = info, addr = buffer.writableData(), rowBytes = rowBytes, underlyingMemoryOwner = buffer)
    }

    fun setColorSpace(colorSpace: ColorSpace?) {
        Stats.onNativeCall()
        maybeSynchronized(_imageInfoLock) {
            _imageInfo = null
            Pixmap_nSetColorSpace(_ptr, getPtr(colorSpace))
        }
        reachabilityBarrier(this)
        reachabilityBarrier(colorSpace)
    }

    fun extractSubset(subsetPtr: NativePointer, area: IRect): Boolean {
        return try {
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

    fun extractSubset(subset: Pixmap, area: IRect): Boolean {
        return extractSubset(subset._ptr, area)
    }

    val info: ImageInfo
        get() {
            Stats.onNativeCall()
            return try {
                maybeSynchronized(_imageInfoLock) {
                    if (_imageInfo == null) {
                        _imageInfo = ImageInfo.createUsing(
                            _ptr = _ptr,
                            _nGetImageInfo = ::Pixmap_nGetInfo
                        )
                    }
                    _imageInfo!!
                }
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
                Pixmap_nGetAddr(_ptr)
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
            Pixmap_nGetAlphaF(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getAddr(x: Int, y: Int): NativePointer {
        Stats.onNativeCall()
        return try {
            Pixmap_nGetAddrAt(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun readPixels(info: ImageInfo, addr: NativePointer, rowBytes: Int): Boolean {
        Stats.onNativeCall()
        return try {
            Pixmap_nReadPixels(
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
            Pixmap_nReadPixelsFromPoint(
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
            maybeSynchronized(_imageInfoLock) {
                _imageInfo = null
                Pixmap_nReadPixelsToPixmap(
                    _ptr,
                    getPtr(pixmap)
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    fun readPixels(pixmap: Pixmap, srcX: Int, srcY: Int): Boolean {
        Stats.onNativeCall()
        return try {
            maybeSynchronized(pixmap._imageInfoLock) {
                pixmap._imageInfo = null
                Pixmap_nReadPixelsToPixmapFromPoint(
                    _ptr,
                    getPtr(pixmap),
                    srcX,
                    srcY
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    fun scalePixels(dstPixmap: Pixmap?, samplingMode: SamplingMode): Boolean {
        Stats.onNativeCall()
        return try {
            Pixmap_nScalePixels(
                _ptr,
                getPtr(dstPixmap),
                samplingMode._packedInt1(),
                samplingMode._packedInt2()
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dstPixmap)
        }
    }

    fun erase(color: Int): Boolean {
        Stats.onNativeCall()
        return try {
            Pixmap_nErase(_ptr, color)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun erase(color: Int, subset: IRect): Boolean {
        Stats.onNativeCall()
        return try {
            Pixmap_nEraseSubset(
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

    val buffer: Data
        get() = (underlyingMemoryOwner as? Data) ?: Data.makeWithoutCopy(
            memoryAddr = addr,
            length = computeByteSize(),
            underlyingMemoryOwner = this
        )

    private object _FinalizerHolder {
        val PTR = Pixmap_nGetFinalizer()
    }

    companion object {
        fun make(info: ImageInfo, buffer: Data, rowBytes: Int): Pixmap {
            return make(info, buffer.writableData(), rowBytes, underlyingMemoryOwner = buffer)
        }

        fun make(info: ImageInfo, addr: NativePointer, rowBytes: Int, underlyingMemoryOwner: Managed? = null): Pixmap {
            return try {
                val ptr = Pixmap_nMake(
                    info.width, info.height,
                    info.colorInfo.colorType.ordinal,
                    info.colorInfo.alphaType.ordinal,
                    getPtr(info.colorInfo.colorSpace), addr, rowBytes
                )
                require(ptr != NullPointer) { "Failed to create Pixmap." }
                Pixmap(ptr, true).also {
                    it.underlyingMemoryOwner = underlyingMemoryOwner
                }
            } finally {
                reachabilityBarrier(info.colorInfo.colorSpace)
            }
        }
    }
}