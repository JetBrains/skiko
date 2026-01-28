package org.jetbrains.skia.util

import org.jetbrains.skia.impl.NativePointer
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal expect val NativePointer.isNullPointer: Boolean

fun assertIsNullPointer(ptr: NativePointer) =
    assertTrue(ptr.isNullPointer, message = "Expected a null pointer")

fun assertIsNotNullPointer(ptr: NativePointer) =
    assertFalse(ptr.isNullPointer, message = "Expected a non-null pointer")
