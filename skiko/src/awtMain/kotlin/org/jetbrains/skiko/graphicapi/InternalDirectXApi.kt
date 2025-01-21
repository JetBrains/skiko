package org.jetbrains.skiko.graphicapi

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.GpuPriority
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Library
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.isVideoCardSupported

internal object InternalDirectXApi {
    init {
        Library.load()
    }

    private external fun getTextureAlignment(): Long
    private val rowBytesAlignment = getTextureAlignment().toInt()
    private val widthSizeAlignment = rowBytesAlignment / 4

    /**
     * Calculate aligned width/height that is needed for performance optimization,
     * since DirectX uses aligned bytebuffer.
     */
    fun alignedTextureWidth(width: Int) = if (width % widthSizeAlignment != 0) {
        width + widthSizeAlignment - (width % widthSizeAlignment);
    } else {
        width
    }

    // Called from native code
    private fun isAdapterSupported(name: String) = isVideoCardSupported(GraphicsApi.DIRECT3D, hostOs, name)

    fun chooseAdapter(adapterPriority: GpuPriority): NativePointer = chooseAdapter(adapterPriority.ordinal)
    private external fun chooseAdapter(adapterPriority: Int): NativePointer
    external fun createDirectXOffscreenDevice(adapter: NativePointer): NativePointer
    external fun makeDirectXContext(device: NativePointer): NativePointer

    external fun waitForCompletion(device: NativePointer, texturePtr: NativePointer)
    external fun readPixels(texturePtr: NativePointer, byteArray: ByteArray): Boolean


    /**
     * Provides ID3D12Resource texture taking given [oldTexturePtr] into account
     * since it can be reused if width and height are not changed,
     * or the new one will be created.
     */
    external fun makeDirectXTexture(device: NativePointer, oldTexturePtr: NativePointer, width: Int, height: Int): NativePointer
    external fun disposeDirectXTexture(texturePtr: NativePointer)

    external fun makeDirectXRenderTargetOffScreen(texturePtr: NativePointer): NativePointer

    external fun disposeDevice(device: NativePointer)
}
