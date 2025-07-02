@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skiko.tests

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5")
internal external fun TestGlContext_nFillByteArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5")
internal external fun TestGlContext_nFillFloatArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillShortArrayOf5")
internal external fun TestGlContext_nFillShortArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillIntArrayOf5")
internal external fun TestGlContext_nFillIntArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillDoubleArrayOf5")
internal external fun TestGlContext_nFillDoubleArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nWriteArraysOfInts")
internal external fun TestGlContext_nWriteArraysOfInts(interopPointer: InteropPointer): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__nStringByIndex")
internal external fun TestGlContext_nStringByIndex(index: Int): NativePointer


@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext")
internal external fun TestGlContext_nCreate(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextGetFinalizer")
internal external fun TestGlContext_nGetFinalizer(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent")
internal external fun TestGlContext_nMakeCurrent(ptr: NativePointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers")
internal external fun TestGlContext_nSwapBuffers(ptr: NativePointer)
