package org.jetbrains.skia

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ImportGeneratorConfigurationKeys {

    val PATH: CompilerConfigurationKey<String> = CompilerConfigurationKey.create(
        PATH_OPTION_NAME
    )

    val PREFIX: CompilerConfigurationKey<String> = CompilerConfigurationKey.create(
        PREFIX_OPTION_NAME
    )
}