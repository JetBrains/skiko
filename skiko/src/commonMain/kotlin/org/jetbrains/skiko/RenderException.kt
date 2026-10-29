package org.jetbrains.skiko

/**
 * An exception related to a rendering failure
 * (driver failure, rendering library failure, rendering device failure)
 */
@InternalSkikoApi
open class RenderException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
