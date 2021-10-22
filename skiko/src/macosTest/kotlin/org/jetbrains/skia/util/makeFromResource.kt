package org.jetbrains.skia.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.Typeface
import platform.Foundation.NSURL

private const val RESOURCES_PATH = "src/commonTest/resources"

fun makePath(resourceId: String) = run {
    val filePath = "$RESOURCES_PATH/$resourceId"
    // Remove all '..' and '.'
    val standardized = NSURL.URLWithString(filePath)?.standardizedURL?.absoluteString
    standardized ?: filePath
}

actual suspend fun Typeface.Companion.makeFromResource(resourceId: String, index: Int): Typeface =
    makeFromFile(makePath(resourceId), index)

actual suspend fun Data.Companion.makeFromResource(resourceId: String) =
    makeFromFileName(makePath(resourceId))
