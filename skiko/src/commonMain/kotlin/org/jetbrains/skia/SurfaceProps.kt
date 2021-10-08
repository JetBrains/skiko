package org.jetbrains.skia

class SurfaceProps constructor(
    internal val isDeviceIndependentFonts: Boolean = false,
    internal val pixelGeometry: PixelGeometry = PixelGeometry.UNKNOWN
) {
    constructor(geo: PixelGeometry) : this(false, geo)

    // Used from JNI code.
    private fun _getPixelGeometryOrdinal(): Int {
        return pixelGeometry.ordinal
    }

    fun _getFlags(): Int {
        return 0 or if (isDeviceIndependentFonts) 1 else 0
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SurfaceProps) return false
        if (isDeviceIndependentFonts != other.isDeviceIndependentFonts) return false
        return this.pixelGeometry == other.pixelGeometry
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (isDeviceIndependentFonts) 79 else 97
        result = result * PRIME + pixelGeometry.hashCode()
        return result
    }

    override fun toString(): String {
        return "SurfaceProps(_deviceIndependentFonts=$isDeviceIndependentFonts, _pixelGeometry=$pixelGeometry)"
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