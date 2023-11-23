package org.jetbrains.skia

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ImportGeneratorConfigurationKeys {

    val PATH: CompilerConfigurationKey<String> = CompilerConfigurationKey.create(
        "path"
    )
}