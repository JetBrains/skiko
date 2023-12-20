package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.io.File

internal class ImportGeneratorExtension(
    private val path: String,
    private val prefix: String?
) : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        val outputFile = File(path)
        outputFile.parentFile.mkdirs()
        val prefixFile = prefix?.let { File(it) }
        outputFile.writer().use { writer ->
            prefixFile?.let { writer.appendLine(it.readText()) }
            writer.appendLine("export const {")
            moduleFragment.transformChildren(ImportGeneratorTransformer(), writer)
            writer.appendLine("} = loadedWasm.wasmExports;")
        }
    }
}