package org.jetbrains.skiko

import java.io.File
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class ThreadStateBindingsExtension(
    private val path: String,
    private val moduleDirectory: String
) : IrGenerationExtension {
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val outputFile = File(path)
        outputFile.parentFile.mkdirs()
        outputFile.bufferedWriter().use { writer ->
            for (file in moduleFragment.files) {
                val source = File(file.fileEntry.name).relativeTo(File(moduleDirectory)).invariantSeparatorsPath
                file.acceptChildrenVoid(object : IrVisitorVoid() {
                    override fun visitElement(element: IrElement) {
                        element.acceptChildrenVoid(this)
                    }

                    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
                        if (declaration.isExternal) {
                            val annotation = declaration.getAnnotation(FqName("kotlin.native.SymbolName"))
                            if (annotation != null) {
                                val argument = annotation.argumentMapping[Name.identifier("name")] as? IrConst
                                val symbol = argument?.value as? String
                                    ?: error("Unsupported SymbolName argument on ${declaration.name}")
                                val marked = declaration.hasAnnotation(FqName("kotlin.native.ForceNativeThreadState"))
                                writer.appendLine("$symbol ${if (marked) 1 else 0} $source")
                            }
                        }
                        declaration.acceptChildrenVoid(this)
                    }
                })
            }
        }
    }
}
