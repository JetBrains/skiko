package org.jetbrains.skiko

internal class RenderException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
