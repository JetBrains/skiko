package org.jetbrains.skija

interface IHasImageInfo {
    val imageInfo: ImageInfo

    /**
     * Returns pixel count in each row. Should be equal or less than
     * getRowBytes() / getImageInfo().getBytesPerPixel().
     *
     * May be less than getPixelRef().getWidth(). Will not exceed getPixelRef().getWidth() less
     *
     * @return  pixel width in ImageInfo
     */
    val width: Int
        get() = imageInfo.width

    /**
     * Returns pixel row count.
     *
     * Maybe be less than getPixelRef().getHeight(). Will not exceed getPixelRef().getHeight()
     *
     * @return  pixel height in ImageInfo
     */
    val height: Int
        get() = imageInfo.height
    val colorInfo: ColorInfo
        get() = imageInfo.colorInfo
    val colorType: ColorType
        get() = imageInfo.colorInfo.colorType
    val alphaType: ColorAlphaType
        get() = imageInfo.colorInfo.alphaType
    val colorSpace: ColorSpace?
        get() = imageInfo.colorInfo.colorSpace

    /**
     * Returns number of bytes per pixel required by ColorType.
     * Returns zero if colorType is [ColorType.UNKNOWN].
     *
     * @return  bytes in pixel
     */
    val bytesPerPixel: Int
        get() = imageInfo.bytesPerPixel

    /**
     * Returns bit shift converting row bytes to row pixels.
     * Returns zero for [ColorType.UNKNOWN].
     *
     * @return  one of: 0, 1, 2, 3; left shift to convert pixels to bytes
     */
    val shiftPerPixel: Int
        get() = imageInfo.shiftPerPixel

    /**
     * Returns true if either getWidth() or getHeight() are zero.
     *
     * Does not check if PixelRef is null; call [Bitmap.drawsNothing] to check
     * getWidth(), getHeight(), and PixelRef.
     *
     * @return  true if dimensions do not enclose area
     */
    val isEmpty: Boolean
        get() = imageInfo.isEmpty

    /**
     *
     * Returns true if ColorAlphaType is set to hint that all pixels are opaque; their
     * alpha value is implicitly or explicitly 1.0. If true, and all pixels are
     * not opaque, Skia may draw incorrectly.
     *
     *
     * Does not check if SkColorType allows alpha, or if any pixel value has
     * transparency.
     *
     * @return  true if ImageInfo ColorAlphaType is [ColorAlphaType.OPAQUE]
     */
    val isOpaque: Boolean
        get() = imageInfo.colorInfo.isOpaque
}