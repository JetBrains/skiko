package org.jetbrains.skiko

internal class RenderException(
    message: String? = null,
    cause: Exception? = null
) : RuntimeException(message, cause)
