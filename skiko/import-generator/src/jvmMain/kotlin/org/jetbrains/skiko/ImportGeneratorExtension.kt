package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import java.io.File

internal class ImportGeneratorExtension(
    private val path: String,
    private val prefix: String?,
    private val reexportPath: String?,
    private val moduleName: String
) : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext,
    ) {
        val outputFile = File(path)
        outputFile.parentFile.mkdirs()
        val prefixFile = prefix?.let { File(it) }
        val importGenerator = ImportGeneratorTransformer(pluginContext, moduleName)

        outputFile.writer().use { writer ->
            prefixFile?.let {
                val prefixContent = it.readText()
                val inlined = inlineLocalImports(prefixContent, it.parentFile)
                writer.appendLine(inlined)
            }
            moduleFragment.transformChildrenVoid(importGenerator)

            importGenerator.getExportSymbols().forEach { symbolName ->
                writer.appendLine("export let ${symbolName} = (...a) => ($symbolName = loadedWasm._[\"${symbolName}\"])(...a)")
            }
        }

        if (reexportPath == null) return

        val reexportFile = File(reexportPath)
        reexportFile.parentFile.mkdirs()

        reexportFile.writer().use { reexportWriter ->
            reexportWriter.appendLine("import * as wasmApi from \"./$moduleName.mjs\";")
            if (moduleName == "skiko") {
                reexportWriter.appendLine("window['GL'] = wasmApi.GL;")
                reexportWriter.appendLine("export const api = { awaitSkiko: wasmApi.awaitSkiko }")
            }
            else {
                reexportWriter.appendLine("export const isSideModuleLoaded = () => wasmApi.isSideModuleLoaded();")
            }

            importGenerator.getExportSymbols().forEach { symbolName ->
                reexportWriter.appendLine("window['${symbolName}'] = wasmApi['${symbolName}'];")
            }
        }
    }

    private fun inlineLocalImports(content: String, baseDir: File): String {
        val namedImportRegex = Regex("""import\s+\{([^}]+)}\s+from\s+"\.\/([^"]+)";""")
        val bareImportRegex = Regex("""import\s+"\.\/([^"]+)";""")
        var result = content
        for (match in bareImportRegex.findAll(content)) {
            val fileName = match.groupValues[1]
            val importedFile = baseDir.resolve(fileName)
            if (importedFile.exists()) {
                val fileContent = importedFile.readText()
                result = result.replace(match.value, fileContent)
            }
        }
        for (match in namedImportRegex.findAll(content)) {
            val fileName = match.groupValues[2]
            val importedFile = baseDir.resolve(fileName)
            if (importedFile.exists()) {
                // Replace "export var" with "var" in the inlined content
                val fileContent = importedFile.readText()
                    .replace("export var ", "var ")
                result = result.replace(match.value, fileContent)
            }
        }
        return result
    }
}
