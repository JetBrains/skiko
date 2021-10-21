package org.jetbrains.skia.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.Typeface
import org.jetbrains.skiko.loadResourceAsBytes

actual suspend inline fun Typeface.Companion.makeFromResource(resourceId: String, index: Int): Typeface =
    makeFromData(Data.makeFromResource(resourceId), index)

actual suspend inline fun Data.Companion.makeFromResource(resourceId: String) =
    makeFromBytes(loadResourceAsBytes(resourceId))
