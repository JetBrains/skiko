package org.jetbrains.skiko

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.collections.plus

internal class ImportGeneratorTransformer(private val pluginContext: IrPluginContext) : IrElementTransformerVoid() {

    private val exportSymbols = mutableListOf<String>()
    fun getExportSymbols(): List<String> = exportSymbols

    @Suppress("UNCHECKED_CAST")
    private fun IrConstructorCall.getStringValue(value: String): String =
        (getValueArgument(Name.identifier(value)) as IrConst<String>).value

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun IrFunction.addWasmImportAnnotation(name: String) {
        val annotationClass = pluginContext.referenceClass(
            ClassId.fromString("kotlin/wasm/WasmImport") // Replace with your fully qualified annotation name
        ) ?: return

        val ctor = annotationClass.owner.constructors.first()

        val annotationCall = IrConstructorCallImpl.fromSymbolOwner(
            startOffset = startOffset,
            endOffset = endOffset,
            type = annotationClass.owner.defaultType,
            constructorSymbol = ctor.symbol,
        )

        val moduleName = if (name.startsWith("org_jetbrains_skiko_tests_")) "./skiko-test.mjs" else "./skiko.mjs"

        annotationCall.putValueArgument(0, IrConstImpl.string(
            startOffset,
            endOffset,
            pluginContext.irBuiltIns.stringType,
            moduleName
        ))

        annotationCall.putValueArgument(1, IrConstImpl.string(
            startOffset,
            endOffset,
            pluginContext.irBuiltIns.stringType,
            name
        ))

        annotations += annotationCall
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        return super.visitFunction(declaration).apply {
            if (this !is IrFunction) return@apply

            val jsNameAnnotation = getAnnotation(FqName("kotlin.js.JsName"))
                ?: return@apply

            val name = jsNameAnnotation.getStringValue("name")
            addWasmImportAnnotation(name)

            exportSymbols.add(name)
        }
    }
}
