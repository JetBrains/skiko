package org.jetbrains.skiko

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.io.OutputStreamWriter

internal class ImportGeneratorTransformer : IrElementTransformer<OutputStreamWriter> {

    @Suppress("UNCHECKED_CAST")
    private fun IrConstructorCall.getStringValue(value: String): String =
        (getValueArgument(Name.identifier(value)) as IrConst<String>).value

    override fun visitFunction(declaration: IrFunction, data: OutputStreamWriter): IrStatement {

        return super.visitFunction(declaration, data).apply {
            val wasmImportAnnotation = declaration.getAnnotation(FqName("kotlin.wasm.WasmImport"))
                ?: return@apply

            val jsNameAnnotation = declaration.getAnnotation(FqName("kotlin.js.JsName"))
                ?: return@apply

            val name = jsNameAnnotation.getStringValue("name")
            data.appendLine("export let ${name} = (...args) => { $name = loadedWasm.wasmExports[\"${name}\"]; return $name(...args)}")
        }
    }
}