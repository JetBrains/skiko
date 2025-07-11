package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import java.io.OutputStreamWriter

class ReexportGeneratorTransformer(private val pluginContext: IrPluginContext) : IrElementTransformer<OutputStreamWriter> {
    override fun visitFile(
        declaration: IrFile,
        data: OutputStreamWriter
    ): IrFile {
        return super.visitFile(declaration, data).apply {
            data.appendLine("// FILE: ${declaration.name}")
        }
    }
}

