package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.InternalSkikoApi

internal expect fun <R> commonSynchronized(lock: Any, block: () -> R)

internal expect fun defaultLanguageTag(): String

expect class Pattern {
    fun split(input: CharSequence): Array<String>
    fun matcher(input: CharSequence): Matcher
}

expect class Matcher {
    // Named groups are not supported in k/n. That's why we can use only numeric groups
    fun group(ix: Int): String?
    fun matches(): Boolean
}

internal expect fun compilePattern(regex: String): Pattern

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class ExternalSymbolName(val name: String)

/**
 * Switches the calling thread from the Runnable to the Native state around a direct call to an
 * [ExternalSymbolName] function on Kotlin/Native, so the GC can proceed while the call blocks.
 * A foreign exception that escapes the function terminates the program.
 * Has no effect on other platforms.
 *
 * Without it, a binding that blocks can deadlock the GC on Apple platforms: the native code waits
 * for the Objective-C weak-reference lock while the thread that holds this lock waits for the GC.
 *
 * Mark a binding if its native code, directly or through Skia, can:
 * - call Objective-C, CoreFoundation, CoreText, CoreGraphics or Metal, including the release
 *   of their objects, such as the last unref of a typeface, font manager, GPU surface or image;
 * - match fonts, create typefaces, read font names or tables, map text to glyphs, or measure,
 *   shape, lay out or draw text, because a glyph cache miss calls the platform font API;
 * - create a GPU context, surface or image, wrap backend GPU objects, draw or composite into a
 *   canvas that can be GPU-backed, read or write GPU pixels, or flush or submit GPU work;
 * - take a Skia lock that is held across such calls, such as the glyph cache lock or the
 *   paragraph cache lock;
 * - call back into Kotlin, because the callback bridge switches the thread to the Native state
 *   when it returns;
 * - wait for another thread.
 *
 * Other bindings stay unmarked: getters and setters of plain values, geometry, and CPU work without text.
 */
@InternalSkikoApi
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.FUNCTION)
expect annotation class ForceNativeThreadState()

@OptIn(ExperimentalSkikoApi::class)
internal expect fun nMakeVulkanImpl(
    instancePtr: NativePointer,
    physicalDevicePtr: NativePointer,
    devicePtr: NativePointer,
    queuePtr: NativePointer,
    graphicsQueueIndex: Int,
    instanceProcAddr: NativePointer,
    deviceProcAddr: NativePointer,
    apiVersion: Int,
    memoryAllocator: VulkanMemoryAllocator?
): NativePointer