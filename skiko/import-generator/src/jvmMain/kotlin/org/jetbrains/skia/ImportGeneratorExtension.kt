package org.jetbrains.skia

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.io.File

internal class ImportGeneratorExtension(private val path: String) : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        val outputDir = File(path)
        outputDir.mkdir()
        val outputFile = outputDir.resolve("setup.mjs")
        outputFile.writer().use { writer ->
            writer.appendLine("// This file is merged with skiko.mjs by emcc")
            writer.appendLine()
            writer.appendLine("export const { _callCallback, _registerCallback, _releaseCallback, _createLocalCallbackScope, _releaseLocalCallbackScope } = SkikoCallbacks;")
            writer.appendLine()
            writer.appendLine("const loadedWasm = await loadSkikoWASM()")
            writer.appendLine()
            writer.appendLine("export const { GL } = loadedWasm;")
            writer.appendLine()
            writer.appendLine("export const {")
            moduleFragment.transformChildren(ImportGeneratorTransformer(), writer)
            writer.appendLine("} = loadedWasm.wasmExports;")
        }
    }
}