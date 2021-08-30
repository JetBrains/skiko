package org.jetbrains.skija.skottie

import org.jetbrains.annotations.ApiStatus

enum class LogLevel {
    WARNING, ERROR;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}