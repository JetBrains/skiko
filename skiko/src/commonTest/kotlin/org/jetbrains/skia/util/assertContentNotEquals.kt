package org.jetbrains.skia.util

import kotlin.test.assertTrue

internal fun assertContentDifferent(array1: ByteArray, array2: ByteArray, message: String? = null) {
    assertTrue(
        actual = array1.size != array2.size ||
                array1.asSequence().zip(array2.asSequence()).any { it.first != it.second },
        message = message
    )
}
