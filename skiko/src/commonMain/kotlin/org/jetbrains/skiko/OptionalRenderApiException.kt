package org.jetbrains.skiko

/**
 * An exception related to inability of loading an optional rendering API
 * (e.g. ANGLE on Windows)
 */
internal class OptionalRenderApiException(
    message: String? = null,
    cause: Throwable? = null
) : RenderException(message, cause)
