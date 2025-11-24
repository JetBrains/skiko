package org.jetbrains.skiko

import java.lang.RuntimeException

class LibraryLoadException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)