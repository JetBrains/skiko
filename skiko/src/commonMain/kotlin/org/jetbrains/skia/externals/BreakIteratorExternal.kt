@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetFinalizer")
internal external fun BreakIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nMake")
internal external fun BreakIterator_nMake(type: Int, locale: InteropPointer, errorCode: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nCurrent")
internal external fun BreakIterator_nCurrent(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nNext")
internal external fun BreakIterator_nNext(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nPrevious")
internal external fun BreakIterator_nPrevious(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nFirst")
internal external fun BreakIterator_nFirst(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nLast")
internal external fun BreakIterator_nLast(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nPreceding")
internal external fun BreakIterator_nPreceding(ptr: NativePointer, offset: Int): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nFollowing")
internal external fun BreakIterator_nFollowing(ptr: NativePointer, offset: Int): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nIsBoundary")
internal external fun BreakIterator_nIsBoundary(ptr: NativePointer, offset: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetRuleStatus")
internal external fun BreakIterator_nGetRuleStatus(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetRuleStatusesLen")
internal external fun BreakIterator_nGetRuleStatusesLen(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetRuleStatuses")
internal external fun BreakIterator_nGetRuleStatuses(ptr: NativePointer, result: InteropPointer, len: Int)

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nSetText")
internal external fun BreakIterator_nSetText(ptr: NativePointer, textStr: InteropPointer, len: Int, errorCode: InteropPointer): NativePointer
