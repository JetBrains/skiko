package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@OptIn(ExperimentalSkikoApi::class)
internal actual fun makeTestGraphiteContext(): GraphiteContext? = when (hostOs) {
    OS.MacOS -> makeTestMetalContext()
    else -> null
}

@OptIn(ExperimentalSkikoApi::class)
private fun makeTestMetalContext(): GraphiteContext {
    GraphiteLibrary.load()
    val metalObjects = _nCreateMetalObjects()
    check(metalObjects.size == 2) { "Failed to create test Metal objects" }
    return try {
        GraphiteContext.makeMetal(metalObjects[0], metalObjects[1])
    } finally {
        _nReleaseMetalObjects(metalObjects[0], metalObjects[1])
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteTestHelpers__1nCreateMetalObjects")
private external fun _nCreateMetalObjects(): LongArray

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteTestHelpers__1nReleaseMetalObjects")
private external fun _nReleaseMetalObjects(devicePtr: NativePointer, queuePtr: NativePointer)
