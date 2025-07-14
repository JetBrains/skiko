package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import java.io.File

internal class ImportGeneratorExtension(
    private val path: String,
    private val prefix: String?,
    private val reexportPath: String
) : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        val outputFile = File(path)
        outputFile.parentFile.mkdirs()
        val prefixFile = prefix?.let { File(it) }
        val importGenerator = ImportGeneratorTransformer(pluginContext)

        outputFile.writer().use { writer ->
            prefixFile?.let { writer.appendLine(it.readText()) }
            moduleFragment.transformChildrenVoid(importGenerator)

            importGenerator.getExportSymbols().forEach { symbolName ->
                writer.appendLine("export let ${symbolName} = (...a) => ($symbolName = loadedWasm._[\"${symbolName}\"])(...a)")
            }
        }

        val reexportFile = File(reexportPath)
        reexportFile.parentFile.mkdirs()

        reexportFile.writer().use { reexportWriter ->
            reexportWriter.appendLine("import * as wasmApi from \"./skiko.mjs\";")
            reexportWriter.appendLine("window['GL'] = wasmApi.GL;")
            reexportWriter.appendLine("export const api = { awaitSkiko: wasmApi.awaitSkiko }")

            importGenerator.getExportSymbols().forEach { symbolName ->
                reexportWriter.appendLine("window['${symbolName}'] = wasmApi['${symbolName}'];")
            }
        }
    }
}

