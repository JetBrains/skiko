package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class ThreadStateBindingsRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true
    override val pluginId: String = "skiko-thread-state-bindings"

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(
            ThreadStateBindingsExtension(
                configuration.get(ThreadStateBindingsConfigurationKeys.PATH)!!,
                configuration.get(ThreadStateBindingsConfigurationKeys.MODULE_DIRECTORY)!!
            )
        )
    }
}
