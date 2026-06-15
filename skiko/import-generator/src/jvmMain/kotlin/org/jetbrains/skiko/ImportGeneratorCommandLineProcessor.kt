package org.jetbrains.skiko

import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.skiko.ImportGeneratorConfigurationKeys.PATH
import org.jetbrains.skiko.ImportGeneratorConfigurationKeys.PREFIX
import org.jetbrains.skiko.ImportGeneratorConfigurationKeys.REEXPORT_PATH
import org.jetbrains.skiko.ImportGeneratorConfigurationKeys.MODULE_NAME

@OptIn(ExperimentalCompilerApi::class)
class ImportGeneratorCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "org.jetbrains.skiko.imports.generator"
    override val pluginOptions = listOf(PATH_OPTION, PREFIX_OPTION, REEXPORT_OPTION, MODULE_NAME_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) =
        when (option) {
            PATH_OPTION -> configuration.put(PATH, value)
            PREFIX_OPTION -> configuration.put(PREFIX, value)
            REEXPORT_OPTION -> configuration.put(REEXPORT_PATH, value)
            MODULE_NAME_OPTION -> configuration.put(MODULE_NAME, value)
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

        val REEXPORT_OPTION = CliOption(
            REEXPORT_OPTION_NAME, "<path>",
            "reexport",
            required = false, allowMultipleOccurrences = false
        )

        val MODULE_NAME_OPTION = CliOption(
            MODULE_NAME_OPTION_NAME, "<module>",
            "target module base name",
            required = false, allowMultipleOccurrences = false
        )
    }
}
