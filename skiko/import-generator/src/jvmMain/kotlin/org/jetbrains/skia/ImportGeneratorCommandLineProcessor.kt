package org.jetbrains.skia

import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.skia.ImportGeneratorConfigurationKeys.PATH
import org.jetbrains.skia.ImportGeneratorConfigurationKeys.PREFIX

@OptIn(ExperimentalCompilerApi::class)
class ImportGeneratorCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "org.jetbrains.skia.import.generator"
    override val pluginOptions = listOf(PATH_OPTION, PREFIX_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) =
        when (option) {
            PATH_OPTION -> configuration.put(PATH, value)
            PREFIX_OPTION -> configuration.put(PREFIX, value)
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }

    companion object {
        val PATH_OPTION = CliOption(
            PATH_OPTION_NAME, "<path>",
            "path",
            required = true, allowMultipleOccurrences = false
        )

        val PREFIX_OPTION = CliOption(
            PREFIX_OPTION_NAME, "<path>",
            "prefix",
            required = false, allowMultipleOccurrences = false
        )
    }
}