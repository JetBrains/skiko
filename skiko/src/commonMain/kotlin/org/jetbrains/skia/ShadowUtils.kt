package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

object ShadowUtils {
    /**
     * Draw an offset spot shadow and outlining ambient shadow for the given path using a disc
     * light. The shadow may be cached, depending on the path type and canvas matrix. If the
     * matrix is perspective or the path is volatile, it will not be cached.
     *
     * @param canvas               The canvas on which to draw the shadows.
     * @param path                 The occluder used to generate the shadows.
     * @param zPlaneParams         Values for the plane function which returns the Z offset of the
     * occluder from the canvas based on local x and y values (the current matrix is not applied).
     * @param lightPos             The 3D position of the light relative to the canvas plane. This is
     * independent of the canvas's current matrix.
     * @param lightRadius          The radius of the disc light.
     * @param ambientColor         The color of the ambient shadow.
     * @param spotColor            The color of the spot shadow.
     * @param transparentOccluder  The occluding object is not opaque. Knowing that the occluder is opaque allows
     * us to cull shadow geometry behind it and improve performance.
     * @param geometricOnly        Don't try to use analytic shadows.
     */
    fun drawShadow(
        canvas: Canvas,
        path: Path,
        zPlaneParams: Point3,
        lightPos: Point3,
        lightRadius: Float,
        ambientColor: Int,
        spotColor: Int,
        transparentOccluder: Boolean,
        geometricOnly: Boolean
    ) {
        Stats.onNativeCall()
        var flags = 0
        if (transparentOccluder) flags = flags or 1
        if (geometricOnly) flags = flags or 2
        try {
            _nDrawShadow(
                getPtr(canvas),
                getPtr(path),
                zPlaneParams.x,
                zPlaneParams.y,
                zPlaneParams.z,
                lightPos.x,
                lightPos.y,
                lightPos.z,
                lightRadius,
                ambientColor,
                spotColor,
                flags
            )
        } finally {
            reachabilityBarrier(canvas)
            reachabilityBarrier(path)
        }
    }

    /**
     * Helper routine to compute ambient color value for one-pass tonal alpha.
     *
     * @param ambientColor   Original ambient color
     * @param spotColor      Original spot color
     * @return               Modified ambient color
     */
    fun computeTonalAmbientColor(ambientColor: Int, spotColor: Int): Int {
        Stats.onNativeCall()
        return _nComputeTonalAmbientColor(ambientColor, spotColor)
    }

    /**
     * Helper routine to compute spot color value for one-pass tonal alpha.
     *
     * @param ambientColor   Original ambient color
     * @param spotColor      Original spot color
     * @return               Modified spot color
     */
    fun computeTonalSpotColor(ambientColor: Int, spotColor: Int): Int {
        Stats.onNativeCall()
        return _nComputeTonalSpotColor(ambientColor, spotColor)
    }

    init {
        staticLoad()
    }
}


@ExternalSymbolName("org_jetbrains_skia_ShadowUtils__1nDrawShadow")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ShadowUtils__1nDrawShadow")
private external fun _nDrawShadow(
    canvasPtr: NativePointer,
    pathPtr: NativePointer,
    zPlaneX: Float,
    zPlaneY: Float,
    zPlaneZ: Float,
    lightPosX: Float,
    lightPosY: Float,
    lightPosZ: Float,
    lightRadius: Float,
    ambientColor: Int,
    spotColor: Int,
    flags: Int
)


@ExternalSymbolName("org_jetbrains_skia_ShadowUtils__1nComputeTonalAmbientColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ShadowUtils__1nComputeTonalAmbientColor")
private external fun _nComputeTonalAmbientColor(ambientColor: Int, spotColor: Int): Int

@ExternalSymbolName("org_jetbrains_skia_ShadowUtils__1nComputeTonalSpotColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ShadowUtils__1nComputeTonalSpotColor")
private external fun _nComputeTonalSpotColor(ambientColor: Int, spotColor: Int): Int
