package org.jetbrains.skiko

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.io.OutputStreamWriter

internal class ImportGeneratorTransformer : IrElementTransformer<OutputStreamWriter> {
    override fun visitFunction(declaration: IrFunction, data: OutputStreamWriter): IrStatement {

        return super.visitFunction(declaration, data).apply {
            val wasmImportAnnotation = declaration.getAnnotation(FqName("kotlin.wasm.WasmImport"))
                ?: return@apply

            val jsNameAnnotation = declaration.getAnnotation(FqName("kotlin.js.JsName"))
                ?: return@apply

            @Suppress("UNCHECKED_CAST")
            val const = wasmImportAnnotation.getValueArgument(Name.identifier("name")) as IrConst<String>
            data.appendLine("export const ${const.value} = loadedWasm.wasmExports[\"${const.value}\"];")
        }
    }
}