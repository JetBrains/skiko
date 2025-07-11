package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.io.OutputStreamWriter

class ReexportGeneratorTransformer(private val pluginContext: IrPluginContext) : IrElementTransformer<OutputStreamWriter> {

    @Suppress("UNCHECKED_CAST")
    private fun IrConstructorCall.getStringValue(value: String): String =
        (getValueArgument(Name.identifier(value)) as IrConst<String>).value

    override fun visitFunction(declaration: IrFunction, data: OutputStreamWriter): IrStatement {

        return super.visitFunction(declaration, data).apply {
            if (this !is IrFunction) return@apply

            val jsNameAnnotation = getAnnotation(FqName("kotlin.js.JsName"))
                ?: return@apply

            val name = jsNameAnnotation.getStringValue("name")

            data.appendLine("window['${name}'] = wasmApi['${name}'];")
        }
    }

}

