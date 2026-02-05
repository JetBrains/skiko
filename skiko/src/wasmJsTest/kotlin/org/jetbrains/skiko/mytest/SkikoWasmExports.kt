package org.jetbrains.skiko.mytest

import kotlinx.coroutines.await
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Data
import org.jetbrains.skia.impl._malloc
import org.jetbrains.skiko.tests.runTest
import org.jetbrains.skiko.wasm.awaitSkiko
import org.khronos.webgl.ArrayBuffer
import kotlin.test.Test

class SkikoWasmExports {

    @Test
    fun canGetSkikoWasmArrayBuffer() = runTest {
        val skikoWasm = awaitSkiko.await<JsAny>()
        val ptr = _malloc(1000)
        println("ptr = $ptr\n")

        val ptr2 = _malloc(1000)
        println("ptr2 = $ptr2\n")

        val ab = skikoArrayBuffer(skikoWasm)
        println("Length = " + ab.byteLength)
//        val buffer = getSkikoWasmBuffer(skikoWasm)
//        println("Buffer = $buffer")
//        val buffer = skikoMemoryArrayBuffer()
//        println(buffer)
    }
}

private fun consoleLog(obj: JsAny) {
    js("console.log(obj)")
}

private fun getSkikoWasmBuffer(skikoWasm: JsAny): JsAny =
    js("console.log(skikoWasm)")

private fun skikoArrayBuffer(skikoWasm: JsAny): ArrayBuffer = js("({buffer: skikoWasm.wasmExports.memory.buffer})")