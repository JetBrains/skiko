package org.jetbrains.skia

actual open class PaintNativeApi actual constructor() : PaintNativeApiInterface {
    override fun _nGetFinalizer(): Long = PaintApiC._nGetFinalizer()

    override fun _nMake(): Long = PaintApiC._nMake()

    override fun _nMakeClone(ptr: Long): Long = PaintApiC._nMakeClone(ptr)

    override fun _nEquals(ptr: Long, otherPtr: Long): Boolean = PaintApiC._nEquals(ptr, otherPtr)

    override fun _nReset(ptr: Long) = PaintApiC._nReset(ptr)

    override fun _nIsAntiAlias(ptr: Long): Boolean = PaintApiC._nIsAntiAlias(ptr)

    override fun _nSetAntiAlias(ptr: Long, value: Boolean) = PaintApiC._nSetAntiAlias(ptr, value)

    override fun _nIsDither(ptr: Long): Boolean = PaintApiC._nIsDither(ptr)

    override fun _nSetDither(ptr: Long, value: Boolean) = PaintApiC._nSetDither(ptr, value)

    override fun _nGetMode(ptr: Long): Int = PaintApiC._nGetMode(ptr)

    override fun _nSetMode(ptr: Long, value: Int) = PaintApiC._nSetMode(ptr, value)

    override fun _nGetColor(ptr: Long): Int = PaintApiC._nGetColor(ptr)

    override fun _nGetColor4f(ptr: Long): Color4f = PaintApiC._nGetColor4f(ptr)

    override fun _nSetColor(ptr: Long, argb: Int) = PaintApiC._nSetColor(ptr, argb)

    override fun _nSetColor4f(ptr: Long, r: Float, g: Float, b: Float, a: Float, colorSpacePtr: Long)
        = PaintApiC._nSetColor4f(ptr, r, g, b, a, colorSpacePtr)

    override fun _nGetStrokeWidth(ptr: Long): Float = PaintApiC._nGetStrokeWidth(ptr)

    override fun _nSetStrokeWidth(ptr: Long, value: Float) = PaintApiC._nSetStrokeWidth(ptr, value)

    override fun _nGetStrokeMiter(ptr: Long): Float = PaintApiC._nGetStrokeMiter(ptr)

    override fun _nSetStrokeMiter(ptr: Long, value: Float) = PaintApiC._nSetStrokeMiter(ptr, value)

    override fun _nGetStrokeCap(ptr: Long): Int = PaintApiC._nGetStrokeCap(ptr)

    override fun _nSetStrokeCap(ptr: Long, value: Int) = PaintApiC._nSetStrokeCap(ptr, value)

    override fun _nGetStrokeJoin(ptr: Long): Int = PaintApiC._nGetStrokeJoin(ptr)

    override fun _nSetStrokeJoin(ptr: Long, value: Int) = PaintApiC._nSetStrokeJoin(ptr, value)

    override fun _nGetFillPath(ptr: Long, path: Long, resScale: Float): Long = PaintApiC._nGetFillPath(ptr, path, resScale)

    override fun _nGetFillPathCull(
        ptr: Long,
        path: Long,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        resScale: Float
    ): Long = PaintApiC._nGetFillPathCull(ptr, path, left, top, right, bottom, resScale)

    override fun _nGetShader(ptr: Long): Long = PaintApiC._nGetShader(ptr)

    override fun _nSetShader(ptr: Long, shaderPtr: Long) = PaintApiC._nSetShader(ptr, shaderPtr)

    override fun _nGetColorFilter(ptr: Long): Long = PaintApiC._nGetColorFilter(ptr)

    override fun _nSetColorFilter(ptr: Long, colorFilterPtr: Long) = PaintApiC._nSetColorFilter(ptr, colorFilterPtr)

    override fun _nGetBlendMode(ptr: Long): Int = PaintApiC._nGetBlendMode(ptr)

    override fun _nSetBlendMode(ptr: Long, mode: Int) = PaintApiC._nSetBlendMode(ptr, mode)

    override fun _nGetPathEffect(ptr: Long): Long = PaintApiC._nGetPathEffect(ptr)

    override fun _nSetPathEffect(ptr: Long, pathEffectPtr: Long) = PaintApiC._nSetPathEffect(ptr, pathEffectPtr)

    override fun _nGetMaskFilter(ptr: Long): Long = PaintApiC._nGetMaskFilter(ptr)

    override fun _nSetMaskFilter(ptr: Long, filterPtr: Long) = PaintApiC._nSetMaskFilter(ptr, filterPtr)

    override fun _nGetImageFilter(ptr: Long): Long = PaintApiC._nGetImageFilter(ptr)

    override fun _nSetImageFilter(ptr: Long, filterPtr: Long) = PaintApiC._nSetImageFilter(ptr, filterPtr)

    override fun _nHasNothingToDraw(ptr: Long): Boolean = PaintApiC._nHasNothingToDraw(ptr)

}