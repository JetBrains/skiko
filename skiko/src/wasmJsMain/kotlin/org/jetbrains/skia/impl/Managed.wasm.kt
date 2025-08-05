package org.jetbrains.skia.impl

/**
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/FinalizationRegistry
 */
internal external class FinalizationRegistry(cleanup: (JsReference<FinalizationThunk>) -> Unit) {
    /**
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/FinalizationRegistry/register
     */
    fun register(obj: JsReference<Managed>, handle: JsReference<FinalizationThunk>, unregisterToken: JsReference<Managed>)

    /**
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/FinalizationRegistry/unregister
     */
    fun unregister(unregisterToken: JsReference<Managed>)
}

private val registry = FinalizationRegistry { thunk: JsReference<FinalizationThunk> ->
    thunk.get().clean()
}

internal actual fun register(managed: Managed, thunk: FinalizationThunk) {
    val managedRef = managed.toJsReference()
    registry.register(managedRef, thunk.toJsReference(), managedRef)
}

internal actual fun unregister(managed: Managed) {
    registry.unregister(managed.toJsReference())
}