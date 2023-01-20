package org.jetbrains.skia.impl

internal external class FinalizationRegistry(cleanup: (dynamic) -> Unit) {
    fun register(obj: dynamic, handle: dynamic)
    fun unregister(obj: dynamic)
}

private val registry = FinalizationRegistry {
    val thunk = it as FinalizationThunk
    thunk.clean()
}

internal actual fun register(managed: Managed, thunk: FinalizationThunk) {
    registry.register(managed, thunk)
}

internal actual fun unregister(managed: Managed) {
    registry.unregister(managed)
}