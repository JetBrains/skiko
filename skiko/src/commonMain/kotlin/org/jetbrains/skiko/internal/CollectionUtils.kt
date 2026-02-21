package org.jetbrains.skiko.internal

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
internal inline fun <T, R> Array<T>.unpackTo(destinationArray: R, unpackItem: (T, R, Int) -> Int): R {
    contract {
        callsInPlace(unpackItem)
    }
    var targetIndex = 0
    for (i in indices) {
        targetIndex = unpackItem(this[i], destinationArray, targetIndex)
    }
    return destinationArray
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}
@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEachReversed(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices.reversed()) {
        val item = get(index)
        action(item)
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEachIndexed(action: (Int, T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(index, item)
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastAny(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastNone(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return false }
    return true
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastAll(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (!predicate(it)) return false }
    return true
}