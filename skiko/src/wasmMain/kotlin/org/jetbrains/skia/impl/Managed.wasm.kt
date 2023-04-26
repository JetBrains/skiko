package org.jetbrains.skia.impl

internal external class FinalizationRegistry(cleanup: (JsReference<FinalizationThunk>) -> Unit) {
    fun register(obj: JsReference<Managed>, handle: JsReference<FinalizationThunk>)
    fun unregister(obj: JsReference<Managed>)
}

private val registry = FinalizationRegistry { thunk: JsReference<FinalizationThunk> ->
    thunk.get().clean()
}

internal actual fun register(managed: Managed, thunk: FinalizationThunk) {
    registry.register(managed.toJsReference(), thunk.toJsReference())
}

internal actual fun unregister(managed: Managed) {
    registry.unregister(managed.toJsReference())
}