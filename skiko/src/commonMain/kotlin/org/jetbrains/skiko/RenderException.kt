package org.jetbrains.skiko

@InternalSkikoApi
class RenderException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
