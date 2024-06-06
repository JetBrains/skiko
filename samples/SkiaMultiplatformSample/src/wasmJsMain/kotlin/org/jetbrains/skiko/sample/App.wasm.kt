package org.jetbrains.skiko.sample

import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

val notoColorEmoji = "https://storage.googleapis.com/skia-cdn/misc/NotoColorEmoji.ttf"
val notoSancSC = "http://localhost:8080/NotoSansSC-Regular.ttf"

fun main() {
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    val skiaLayer = SkiaLayer()
    val clocks = WebClocks(skiaLayer, canvas)
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, clocks)


    MainScope().launch {
        val notoEmojisBytes = loadRes(notoColorEmoji).toByteArray()
        val notoSansSCBytes = loadRes(notoSancSC).toByteArray()
        val typeface1 = Typeface.makeFromData(Data.makeFromBytes(notoEmojisBytes))
        val typeface2 = Typeface.makeFromData(Data.makeFromBytes(notoSansSCBytes))

        val tfp = TypefaceFontProvider.createExtended().apply {
            registerTypeface(typeface1, extended =  true)
            registerTypeface(typeface2, extended = true)
        }
//        Clocks.fontCollection.setAssetFontManager(tfp)
        Clocks.fontCollection.setDefaultFontManager(FontMgr.makeWrapper(tfp))

        skiaLayer.needRedraw()
    }


//    runApp()
}

internal fun runApp() {
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    val skiaLayer = SkiaLayer()
    val clocks = WebClocks(skiaLayer, canvas)
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, clocks)
    skiaLayer.needRedraw()
}


suspend fun loadRes(url: String): ArrayBuffer {
    return suspendCoroutine { continuation ->
        val req = XMLHttpRequest()
        req.open("GET", url, true)
        req.responseType = "arraybuffer".toJsString().unsafeCast()

        req.onload = { _ ->
            val arrayBuffer = req.response
            if (arrayBuffer is ArrayBuffer) {
                continuation.resume(arrayBuffer)
            } else {
                continuation.resumeWithException(MissingResourceException(url))
            }
        }
        req.send("")
    }
}

private class MissingResourceException(url: String): Exception("GET $url failed")

fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

//@JsFun("""(str) => {
//    return str.codePointAt(0);
//""")
fun codePointForStr(str: String): Int = js("{ return str.codePointAt(0); }")

@JsFun(
    """ (src, size, dstAddr) => {
        const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
        mem8.set(src);
    }
"""
)
internal external fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int)

internal fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
    val size = x.length

    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val memBuffer = allocator.allocate(size)
        val dstAddress = memBuffer.address.toInt()
        jsExportInt8ArrayToWasm(x, size, dstAddress)
        ByteArray(size) { i -> (memBuffer + i).loadByte() }
    }
}