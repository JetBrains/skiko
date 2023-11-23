package org.jetbrains.skia

import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.skia.ImportGeneratorConfigurationKeys.PATH

@OptIn(ExperimentalCompilerApi::class)
class ImportGeneratorCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "org.jetbrains.skia.import.generator"
    override val pluginOptions = listOf(PATH_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) =
        when (option) {
            PATH_OPTION -> configuration.put(PATH, value)
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }

    companion object {
        val PATH_OPTION = CliOption(
            PATH_OPTION_NAME, "<path>",
            "path",
            required = true, allowMultipleOccurrences = false
        )
    }
}