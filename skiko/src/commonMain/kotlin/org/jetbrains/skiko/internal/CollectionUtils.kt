package org.jetbrains.skiko.internal

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
internal inline fun <T> Array<T>.unpackTo(destinationArray: IntArray, unpackItem: (T, IntArray, Int) -> Int): IntArray {
    contract {
        callsInPlace(unpackItem)
    }
    var targetIndex = 0
    for (i in indices) {
        targetIndex = unpackItem(this[i], destinationArray, targetIndex)
    }
    return destinationArray
}