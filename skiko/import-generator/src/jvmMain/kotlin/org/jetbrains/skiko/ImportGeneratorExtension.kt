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
        moduleFragment.transformChildrenVoid(importGenerator)

        outputFile.writer().use { writer ->
            if (prefixFile != null) {
                // prefixFile is de-facto the pre-setupm.mjs, which exports awaitSkiko
                check(moduleName == "skiko") { "We supposed to prepend only skiko.mjs file"}
                writer.appendLine(prefixFile.readText())
            } else {
                check(moduleName != "skiko") { "The core import generator requires a prefix file" }
                writer.appendLine("export { isSideModuleLoaded } from './pre-$moduleName.mjs';")
                writer.appendLine("import { awaitSkiko } from './skiko.mjs';")
            }

            importGenerator.getExportSymbols().flatMap { it.value }.forEach { exportSymbol ->
                    writer.appendLine("export let $exportSymbol")
            }

            writer.appendLine("awaitSkiko.then((module) => {")
            importGenerator.getExportSymbols().flatMap { it.value }.forEach { exportSymbol ->
                writer.appendLine("$exportSymbol = module['_$exportSymbol']")
            }
            writer.appendLine("})")

            writer.appendLine("""
                const origInstantiateStreaming = WebAssembly.instantiateStreaming;
                WebAssembly.instantiateStreaming = function (source, importObject) {
                  if (importObject['./$moduleName.mjs']) {
                        console.log('IMPORT OBJECT MODULE !!!', importObject['./$moduleName.mjs']) 
                        return awaitSkiko.then(() => origInstantiateStreaming.call(WebAssembly, source, importObject));
                  }
                  return origInstantiateStreaming.call(WebAssembly, source, importObject);
                };
            """.trimIndent())

            writer.appendLine("""
                const exportSymbols =  ${importGenerator.getExportSymbols().get(moduleName)!!.serializeAsJsSet()};
                const booleanSymbols = ${importGenerator.getBooleanExportSymbols().serializeAsJsSet()};
                let api = new Proxy(Object.create(null), { get(obj, propName) { return () => {}}});
                
                for (const sym of exportSymbols) { 
                    globalThis.__SKIKO__[sym] = (...a) => api[sym](...a)
                }
                
                awaitSkiko.then((module) => {
                    api = {};
                    for (const sym of exportSymbols) {
                        const raw = module['_' + sym];
                        api[sym] = booleanSymbols.has(sym) ? (...a) => !!raw(...a) : raw;
                    }
                })
            """.trimIndent())
        }

        if (reexportPath == null) return

        val reexportFile = File(reexportPath)
        reexportFile.parentFile.mkdirs()

        reexportFile.writer().use { writer ->
            if (moduleName != "skiko") {
                writer.appendLine("export {isSideModuleLoaded} from './$moduleName.mjs';")
            }
        }
    }
}

private fun Iterable<*>.serializeAsJsSet() = "new Set([${joinToString(", ") { "'$it'" }}])"
