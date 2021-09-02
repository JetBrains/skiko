package org.jetbrains.skia

class SurfaceProps constructor(
    internal val isDeviceIndependentFonts: Boolean = false,
    internal val pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN
) {

    constructor(geo: PixelGeometry) : this(false, geo) {}

    fun _getFlags(): Int {
        return 0 or if (isDeviceIndependentFonts) 1 else 0
    }

    private fun _getPixelGeometryOrdinal(): Int {
        return pixelGeometry.ordinal
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is SurfaceProps) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (isDeviceIndependentFonts != other.isDeviceIndependentFonts) return false
        val `this$_pixelGeometry`: Any = pixelGeometry
        val `other$_pixelGeometry`: Any = other.pixelGeometry
        return if (if (`this$_pixelGeometry` == null) `other$_pixelGeometry` != null else `this$_pixelGeometry` != `other$_pixelGeometry`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is SurfaceProps
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (isDeviceIndependentFonts) 79 else 97
        val `$_pixelGeometry`: Any = pixelGeometry
        result = result * PRIME + (`$_pixelGeometry`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "SurfaceProps(_deviceIndependentFonts=" + isDeviceIndependentFonts + ", _pixelGeometry=" + pixelGeometry + ")"
    }

    fun withDeviceIndependentFonts(_deviceIndependentFonts: Boolean): SurfaceProps {
        return if (isDeviceIndependentFonts == _deviceIndependentFonts) this else SurfaceProps(
            _deviceIndependentFonts,
            pixelGeometry
        )
    }

    fun withPixelGeometry(_pixelGeometry: PixelGeometry): SurfaceProps {
        return if (pixelGeometry == _pixelGeometry) this else SurfaceProps(isDeviceIndependentFonts, _pixelGeometry)
    }
}