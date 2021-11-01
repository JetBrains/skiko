package org.jetbrains.skia.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile
import org.jetbrains.skiko.resourcePath

actual suspend fun Typeface.Companion.makeFromResource(resourceId: String, index: Int): Typeface =
    makeFromFile(resourcePath(resourceId), index)

actual suspend fun Data.Companion.makeFromResource(resourceId: String) =
    makeFromFileName(resourcePath(resourceId))
