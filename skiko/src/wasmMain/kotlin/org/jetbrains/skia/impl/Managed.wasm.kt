package org.jetbrains.skia.impl

internal external class FinalizationRegistry(cleanup: (FinalizationThunk) -> Unit) {
    fun register(obj: Managed, handle: FinalizationThunk)
    fun unregister(obj: Managed)
}

private val registry = FinalizationRegistry {
    it.clean()
}

internal actual fun register(managed: Managed, thunk: FinalizationThunk) {
    registry.register(managed, thunk)
}

internal actual fun unregister(managed: Managed) {
    registry.unregister(managed)
}