package org.jetbrains.skia

actual open class PaintNativeApi actual constructor() : PaintNativeApiInterface {
    override fun _nGetFinalizer(): Long {
        TODO("Not yet implemented")
    }

    override fun _nMake(): Long {
        TODO("Not yet implemented")
    }

    override fun _nMakeClone(ptr: Long): Long {
        TODO("Not yet implemented")
    }

    override fun _nEquals(ptr: Long, otherPtr: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun _nReset(ptr: Long) {
        TODO("Not yet implemented")
    }

    override fun _nIsAntiAlias(ptr: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun _nSetAntiAlias(ptr: Long, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun _nIsDither(ptr: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun _nSetDither(ptr: Long, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun _nGetMode(ptr: Long): Int {
        TODO("Not yet implemented")
    }

    override fun _nSetMode(ptr: Long, value: Int) {
        TODO("Not yet implemented")
    }

    override fun _nGetColor(ptr: Long): Int {
        TODO("Not yet implemented")
    }

    override fun _nGetColor4f(ptr: Long): Color4f {
        TODO("Not yet implemented")
    }

    override fun _nSetColor(ptr: Long, argb: Int) {
        TODO("Not yet implemented")
    }

    override fun _nSetColor4f(ptr: Long, r: Float, g: Float, b: Float, a: Float, colorSpacePtr: Long) {
        TODO("Not yet implemented")
    }

    override fun _nGetStrokeWidth(ptr: Long): Float {
        TODO("Not yet implemented")
    }

    override fun _nSetStrokeWidth(ptr: Long, value: Float) {
        TODO("Not yet implemented")
    }

    override fun _nGetStrokeMiter(ptr: Long): Float {
        TODO("Not yet implemented")
    }

    override fun _nSetStrokeMiter(ptr: Long, value: Float) {
        TODO("Not yet implemented")
    }

    override fun _nGetStrokeCap(ptr: Long): Int {
        TODO("Not yet implemented")
    }

    override fun _nSetStrokeCap(ptr: Long, value: Int) {
        TODO("Not yet implemented")
    }

    override fun _nGetStrokeJoin(ptr: Long): Int {
        TODO("Not yet implemented")
    }

    override fun _nSetStrokeJoin(ptr: Long, value: Int) {
        TODO("Not yet implemented")
    }

    override fun _nGetFillPath(ptr: Long, path: Long, resScale: Float): Long {
        TODO("Not yet implemented")
    }

    override fun _nGetFillPathCull(
        ptr: Long,
        path: Long,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        resScale: Float
    ): Long {
        TODO("Not yet implemented")
    }

    override fun _nGetShader(ptr: Long): Long {
        TODO("Not yet implemented")
    }

    override fun _nSetShader(ptr: Long, shaderPtr: Long) {
        TODO("Not yet implemented")
    }

    override fun _nGetColorFilter(ptr: Long): Long {
        TODO("Not yet implemented")
    }

    override fun _nSetColorFilter(ptr: Long, colorFilterPtr: Long) {
        TODO("Not yet implemented")
    }

    override fun _nGetBlendMode(ptr: Long): Int {
        TODO("Not yet implemented")
    }

    override fun _nSetBlendMode(ptr: Long, mode: Int) {
        TODO("Not yet implemented")
    }

    override fun _nGetPathEffect(ptr: Long): Long {
        TODO("Not yet implemented")
    }

    override fun _nSetPathEffect(ptr: Long, pathEffectPtr: Long) {
        TODO("Not yet implemented")
    }

    override fun _nGetMaskFilter(ptr: Long): Long {
        TODO("Not yet implemented")
    }

    override fun _nSetMaskFilter(ptr: Long, filterPtr: Long) {
        TODO("Not yet implemented")
    }

    override fun _nGetImageFilter(ptr: Long): Long {
        TODO("Not yet implemented")
    }

    override fun _nSetImageFilter(ptr: Long, filterPtr: Long) {
        TODO("Not yet implemented")
    }

    override fun _nHasNothingToDraw(ptr: Long): Boolean {
        TODO("Not yet implemented")
    }
}