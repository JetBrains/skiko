package org.jetbrains.skia

import kotlinx.browser.window
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer

internal actual fun <R> commonSynchronized(lock: Any, block: () -> R) {
    block()
}

actual class Pattern constructor(regex: String) {
    private val _regex = Regex(regex)

    actual fun split(input: CharSequence): Array<String> = _regex.split(input).toTypedArray()
    actual fun matcher(input: CharSequence): Matcher = Matcher(_regex, input)
}

actual class Matcher constructor(private val regex: Regex, private val input: CharSequence) {

    private val matches: Boolean by lazy {
        regex.matches(input)
    }

    private val groups: MatchGroupCollection? by lazy { regex.matchEntire(input)?.groups }

    actual fun group(ix: Int): String? = groups?.get(ix)?.value
    actual fun matches(): Boolean = matches
}

@Suppress("RedundantNullableReturnType")
val LANG: String by lazy {
    val lang: String? = window.navigator.language
    if (lang.isNullOrEmpty()) "en-US" else lang
}

actual fun defaultLanguageTag(): String = LANG

actual fun compilePattern(regex: String): Pattern = Pattern(regex)

internal actual fun nMakeVulkanImpl(
    instancePtr: NativePointer,
    physicalDevicePtr: NativePointer,
    devicePtr: NativePointer,
    queuePtr: NativePointer,
    graphicsQueueIndex: Int,
    instanceProcAddr: NativePointer,
    deviceProcAddr: NativePointer,
    apiVersion: Int,
    memoryAllocator: VulkanMemoryAllocator?
): NativePointer = _nMakeVulkan(
    instancePtr, physicalDevicePtr, devicePtr, queuePtr,
    graphicsQueueIndex, instanceProcAddr, deviceProcAddr, apiVersion,
    NullPointer
)
