@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.icu

import org.jetbrains.skia.ExternalSymbolName

@ExternalSymbolName("org_jetbrains_skia_icu_Unicode__1nCharDirection")
internal external fun Unicode_nCharDirection(codePoint: Int): Int

@ExternalSymbolName("org_jetbrains_skia_icu_Unicode__1nCodePointHasBinaryProperty")
internal external fun Unicode_nCodePointHasBinaryProperty(codePoint: Int, property: Int): Boolean

