package org.jetbrains.skia.impl

/**
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/FinalizationRegistry
 */
internal external class FinalizationRegistry(cleanup: (dynamic) -> Unit) {

    /**
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/FinalizationRegistry/register
     */
    fun register(obj: dynamic, handle: dynamic, unregisterToken: dynamic)

    /**
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/FinalizationRegistry/unregister
     */
    fun unregister(unregisterToken: dynamic)
}

private val registry = FinalizationRegistry {
    val thunk = it as FinalizationThunk
    thunk.clean()
}

internal actual fun register(managed: Managed, thunk: FinalizationThunk) {
    registry.register(managed, thunk, managed)
}

internal actual fun unregister(managed: Managed) {
    registry.unregister(managed)
}