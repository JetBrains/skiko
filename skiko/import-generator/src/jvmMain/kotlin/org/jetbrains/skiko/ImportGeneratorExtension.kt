package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
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
        outputFile.writer().use { writer ->
            writer.appendLine("// REEXPORT PATH ${reexportPath} ===>")
            prefixFile?.let { writer.appendLine(it.readText()) }
            moduleFragment.transformChildren(ImportGeneratorTransformer(pluginContext), writer)
        }

        val reexportFile = File(reexportPath)
        reexportFile.parentFile.mkdirs()

        reexportFile.writer().use { reexportWriter ->
            reexportWriter.appendLine("import * as wasmApi from \"./skiko.mjs\";")
            reexportWriter.appendLine("console.log(wasmApi)");
            reexportWriter.appendLine("window['GL'] = wasmApi.GL;")
//            reexportWriter.appendLine("window['HEAPU8'] = wasmApi.HEAPU8;")
//            reexportWriter.appendLine("window['HEAPU16'] = wasmApi.HEAPU16;")
//            reexportWriter.appendLine("window['HEAPU32'] = wasmApi.HEAPU32;")
//            reexportWriter.appendLine("window['HEAPF32'] = wasmApi.HEAPF32;")
//            reexportWriter.appendLine("window['HEAPF64'] = wasmApi.HEAPF64;")
            reexportWriter.appendLine("export const api = { awaitSkiko: wasmApi.awaitSkiko }")

            moduleFragment.transformChildren(ReexportGeneratorTransformer(pluginContext), reexportWriter)
        }
    }
}

