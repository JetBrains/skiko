package org.jetbrains.skia.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.Typeface

private const val RESOURCES_PATH = "src/jvmTest/resources/fonts"

fun Typeface.Companion.makeFromResource(resourceId: String, index: Int = 0): Typeface =
    makeFromFile("$RESOURCES_PATH/$resourceId", index)

fun Data.Companion.makeFromResource(resourceId: String) = makeFromFileName("$RESOURCES_PATH/$resourceId")
