package org.jetbrains.skiko

/**
 * Load ANGLE library into memory.
 *
 * Should be called before any ANGLE operation.
 *
 * We don't ship ANGLE with Skiko by default. If it's absent in the Skiko native library directory,
 * we'll fallback to other renderers.
 *
 * @throws OptionalRenderApiException if ANGLE library can't be loaded.
 */
internal expect fun loadAngleLibrary()