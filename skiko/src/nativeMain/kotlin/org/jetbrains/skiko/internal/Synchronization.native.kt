package org.jetbrains.skiko.internal

import org.jetbrains.skiko.InternalSkikoApi

@InternalSkikoApi
actual inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R =
    block()
