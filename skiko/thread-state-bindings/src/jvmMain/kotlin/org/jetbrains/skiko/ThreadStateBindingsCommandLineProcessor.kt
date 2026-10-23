package org.jetbrains.skiko

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class ThreadStateBindingsCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "org.jetbrains.skiko.thread.state.bindings"
    override val pluginOptions = listOf(PATH_OPTION, MODULE_DIRECTORY_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) =
        when (option) {
            PATH_OPTION -> configuration.put(ThreadStateBindingsConfigurationKeys.PATH, value)
            MODULE_DIRECTORY_OPTION -> configuration.put(ThreadStateBindingsConfigurationKeys.MODULE_DIRECTORY, value)
            else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
        }

    companion object {
        val PATH_OPTION = CliOption(
            PATH_OPTION_NAME, "<path>", "Bindings output path",
            required = true, allowMultipleOccurrences = false
        )
        val MODULE_DIRECTORY_OPTION = CliOption(
            MODULE_DIRECTORY_OPTION_NAME, "<path>", "Module directory",
            required = true, allowMultipleOccurrences = false
        )
    }
}
