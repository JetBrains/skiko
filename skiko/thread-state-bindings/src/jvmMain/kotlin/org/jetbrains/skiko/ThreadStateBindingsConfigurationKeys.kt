package org.jetbrains.skiko

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ThreadStateBindingsConfigurationKeys {
    val PATH: CompilerConfigurationKey<String> = CompilerConfigurationKey.create(PATH_OPTION_NAME)
    val MODULE_DIRECTORY: CompilerConfigurationKey<String> = CompilerConfigurationKey.create(MODULE_DIRECTORY_OPTION_NAME)
}
