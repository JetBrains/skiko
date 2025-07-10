package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.io.OutputStreamWriter
import kotlin.collections.plus

internal class ImportGeneratorTransformer(private val pluginContext: IrPluginContext) : IrElementTransformer<OutputStreamWriter> {

    @Suppress("UNCHECKED_CAST")
    private fun IrConstructorCall.getStringValue(value: String): String =
        (getValueArgument(Name.identifier(value)) as IrConst<String>).value

    private fun IrFunction.addSkikoJsModuleAnnotation() {
        val annotationClass = pluginContext.referenceClass(
            ClassId.fromString("kotlin/js/JsModule") // Replace with your fully qualified annotation name
        ) ?: return

        val ctor = annotationClass.owner.constructors.first()

        val annotationCall = IrConstructorCallImpl.fromSymbolOwner(
            startOffset = startOffset,
            endOffset = endOffset,
            type = annotationClass.owner.defaultType,
            constructorSymbol = ctor.symbol,
        )

        annotationCall.putValueArgument(0, IrConstImpl.string(
            startOffset,
            endOffset,
            pluginContext.irBuiltIns.stringType,
            "./skiko.mjs"
        ))

        annotations += annotationCall
    }


    override fun visitFunction(declaration: IrFunction, data: OutputStreamWriter): IrStatement {

        return super.visitFunction(declaration, data).apply {
            if (this !is IrFunction) return@apply

            val wasmImportAnnotation = getAnnotation(FqName("kotlin.wasm.WasmImport"))
                ?: return@apply

            val jsNameAnnotation = getAnnotation(FqName("kotlin.js.JsName"))
                ?: return@apply

            addSkikoJsModuleAnnotation()

            val name = jsNameAnnotation.getStringValue("name")
            data.appendLine("export let ${name} = (...args) => { $name = loadedWasm.wasmExports[\"${name}\"]; return $name(...args)}")
        }
    }
}