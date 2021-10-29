package org.jetbrains.skia

import kotlin.test.Test

class PathEffectTest {
    @Test
    fun canCreate() {
        val path = Path.makeFromSVGString("M0 0 L10 10 L10 0 Z")
        val matrix33 = Matrix33(1.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 3.0f)

        val path1 = PathEffect.makeCorner(12.0f)
        PathEffect.makePath1D(path, 1.0f, 10.0f, PathEffect.Style.ROTATE)
        PathEffect.makePath2D(matrix33, path)
        val path2 = PathEffect.makeDash(floatArrayOf(1.0f, 2.0f, 4.0f, 2.0f), 0.0f)
        PathEffect.makeLine2D(2.0f, matrix33)
        PathEffect.makeDiscrete(20.0f, 0.0f, 0)

        path1.makeSum(path2)
        path1.makeCompose(path2)
    }
}