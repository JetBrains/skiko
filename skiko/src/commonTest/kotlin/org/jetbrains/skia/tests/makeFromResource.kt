package org.jetbrains.skia.tests

import org.jetbrains.skia.Data
import org.jetbrains.skia.Typeface

expect suspend fun Typeface.Companion.makeFromResource(resourceId: String, index: Int = 0): Typeface

expect suspend fun Data.Companion.makeFromResource(resourceId: String): Data
