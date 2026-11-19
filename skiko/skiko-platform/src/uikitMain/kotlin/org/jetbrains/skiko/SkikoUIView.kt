@file:OptIn(BetaInteropApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.*
import org.jetbrains.skiko.redrawer.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.Metal.*
import platform.QuartzCore.*
import platform.UIKit.*
import kotlin.native.ref.*

internal expect fun UIView.skikoInitializeUIView(): Unit

@ExportObjCClass
class SkikoUIView : UIView {
    companion object : UIViewMeta() {
        override fun layerClass() = CAMetalLayer
    }

    @Suppress("UNUSED") // required by Objective-C runtime
    @OverrideInit
    constructor(coder: NSCoder) : super(coder) {
        throw UnsupportedOperationException("init(coder: NSCoder) is not supported for SkikoUIView")
    }

    private val _device: MTLDeviceProtocol =
        MTLCreateSystemDefaultDevice() ?: throw IllegalStateException("Metal is not supported on this system")
    private val _metalLayer: CAMetalLayer get() = layer as CAMetalLayer
    private var _skiaLayer: SkiaLayer? = null
    private lateinit var _redrawer: MetalRedrawer

    init {
        skikoInitializeUIView()
        opaque = false // For UIKit interop through a "Hole"

        _metalLayer.also {
            // Workaround for KN compiler bug
            // Type mismatch: inferred type is platform.Metal.MTLDeviceProtocol but objcnames.protocols.MTLDeviceProtocol? was expected
            @Suppress("USELESS_CAST")
            it.device = _device as objcnames.protocols.MTLDeviceProtocol?

            it.pixelFormat = MTLPixelFormatBGRA8Unorm
            doubleArrayOf(0.0, 0.0, 0.0, 0.0).usePinned { pinned ->
                it.backgroundColor = CGColorCreate(CGColorSpaceCreateDeviceRGB(), pinned.addressOf(0))
            }
            it.framebufferOnly = false
        }
    }

    @OptIn(kotlin.experimental.ExperimentalNativeApi::class)
    constructor(
        skiaLayer: SkiaLayer,
        frame: CValue<CGRect> = CGRectNull.readValue()
    ) : super(frame) {
        _skiaLayer = skiaLayer

        val weakSkiaLayer = WeakReference(skiaLayer)

        _redrawer = MetalRedrawer(
            _metalLayer,
            drawCallback = { surface ->
                weakSkiaLayer.get()?.draw(surface)
            }
        )

        skiaLayer.needRedrawCallback = _redrawer::needRender
        skiaLayer.view = this
    }

    internal fun detach() {
        _redrawer.dispose()
    }

    fun load(): SkikoUIView {
        // TODO: redundant, remove in next refactor pass
        return this
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()

        window?.screen?.let {
            contentScaleFactor = it.scale
            _redrawer.maximumFramesPerSecond = it.maximumFramesPerSecond
        }
    }

    override fun layoutSubviews() {
        super.layoutSubviews()

        val scaledSize = bounds.useContents {
            CGSizeMake(size.width * contentScaleFactor, size.height * contentScaleFactor)
        }

        _metalLayer.drawableSize = scaledSize
    }
}
