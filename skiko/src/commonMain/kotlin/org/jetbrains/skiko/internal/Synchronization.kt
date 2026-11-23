package org.jetbrains.skiko.internal

import org.jetbrains.skiko.InternalSkikoApi

@InternalSkikoApi
expect inline fun <R> maybeSynchronized(lock: Any, block: () -> R): R
