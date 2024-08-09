package org.jetbrains.skia.tests

import org.jetbrains.skia.*
import org.jetbrains.skiko.resourcePath

actual suspend fun Typeface.Companion.makeFromResource(resourceId: String, index: Int): Typeface =
    FontMgr.default.makeFromFile(resourcePath(resourceId), index)
        ?: error("Failed to makeFromFile with resourceId = $resourceId")

actual suspend fun Data.Companion.makeFromResource(resourceId: String) =
    makeFromFileName(resourcePath(resourceId))
