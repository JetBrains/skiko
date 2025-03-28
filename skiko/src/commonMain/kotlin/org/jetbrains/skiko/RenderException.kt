package org.jetbrains.skiko

/**
 * An exception related to a rendering failure
 * (driver failure, rendering library failure, rendering device failure)
 */
internal open class RenderException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
