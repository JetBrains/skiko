@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nMakeFromRuntimeEffect")
internal external fun RuntimeShaderBuilder_nMakeFromRuntimeEffect(effectPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nGetFinalizer")
internal external fun RuntimeShaderBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt")
internal external fun RuntimeShaderBuilder_nUniformInt(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt2")
internal external fun RuntimeShaderBuilder_nUniformInt2(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Int, uniformValue2: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt3")
internal external fun RuntimeShaderBuilder_nUniformInt3(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Int, uniformValue2: Int, uniformValue3: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt4")
internal external fun RuntimeShaderBuilder_nUniformInt4(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Int, uniformValue2: Int, uniformValue3: Int, uniformValue4: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat")
internal external fun RuntimeShaderBuilder_nUniformFloat(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat2")
internal external fun RuntimeShaderBuilder_nUniformFloat2(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Float, uniformValue2: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat3")
internal external fun RuntimeShaderBuilder_nUniformFloat3(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Float, uniformValue2: Float, uniformValue3: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat4")
internal external fun RuntimeShaderBuilder_nUniformFloat4(builderPtr: NativePointer, uniformName: InteropPointer, uniformValue1: Float, uniformValue2: Float, uniformValue3: Float, uniformValue4: Float)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatArray")
internal external fun RuntimeShaderBuilder_nUniformFloatArray(builderPtr: NativePointer, uniformName: InteropPointer, uniformFloatArray: InteropPointer, length: Int)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix22")
internal external fun RuntimeShaderBuilder_nUniformFloatMatrix22(builderPtr: NativePointer, uniformName: InteropPointer, uniformMatrix22: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix33")
internal external fun RuntimeShaderBuilder_nUniformFloatMatrix33(builderPtr: NativePointer, uniformName: InteropPointer, uniformMatrix33: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix44")
internal external fun RuntimeShaderBuilder_nUniformFloatMatrix44(builderPtr: NativePointer, uniformName: InteropPointer, uniformMatrix44: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nChildShader")
internal external fun RuntimeShaderBuilder_nChildShader(builderPtr: NativePointer, uniformName: InteropPointer, shaderPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nChildColorFilter")
internal external fun RuntimeShaderBuilder_nChildColorFilter(builderPtr: NativePointer, uniformName: InteropPointer, colorFilterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_RuntimeShaderBuilder__1nMakeShader")
internal external fun RuntimeShaderBuilder_nMakeShader(builderPtr: NativePointer, localMatrix: InteropPointer): NativePointer
