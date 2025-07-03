@file:JsModule("./skiko.mjs")
@file:JsQualifier("wasmExports")
package org.jetbrains.skia.impl

external fun skia_memSetByte(address: NativePointer, value: Byte)

external fun skia_memGetByte(address: NativePointer): Byte

external fun skia_memSetChar(address: NativePointer, value: Char)

external fun skia_memGetChar(address: NativePointer): Char

external fun skia_memSetShort(address: NativePointer, value: Short)

external fun skia_memGetShort(address: NativePointer): Short

external fun skia_memSetInt(address: NativePointer, value: Int)

external fun skia_memGetInt(address: NativePointer): Int

external fun skia_memSetFloat(address: NativePointer, value: Float)

external fun skia_memGetFloat(address: NativePointer): Float

external fun skia_memSetDouble(address: NativePointer, value: Double)

external fun skia_memGetDouble(address: NativePointer): Double
