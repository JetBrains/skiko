package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.MetalRedrawer
import org.robovm.apple.coreanimation.CALayer
import org.robovm.apple.coreanimation.CAMetalLayer
import org.robovm.apple.coregraphics.CGColor
import org.robovm.apple.coregraphics.CGColorSpace
import org.robovm.apple.coregraphics.CGRect
import org.robovm.apple.coregraphics.CGSize
import org.robovm.apple.metal.MTLDevice
import org.robovm.apple.metal.MTLPixelFormat
import org.robovm.apple.uikit.UIView
import org.robovm.objc.annotation.CustomClass
import org.robovm.objc.annotation.Property
import java.lang.ref.WeakReference

@CustomClass("SkikoUIView")
open class SkikoUIView : UIView {

    companion object {
        /**
         * Backs this view with a [CAMetalLayer].
         * Note: must not be named `getLayerClass` — that would clash with the JVM
         * signature of the inherited static [UIView.getLayerClass].
         */
        @JvmStatic
        @Property(selector = "layerClass")
        fun layerClass(): Class<out CALayer> {
            return CAMetalLayer::class.java
        }
    }

    private val _device: MTLDevice = MTLDevice.getSystemDefaultDevice()
        ?: throw IllegalStateException("Metal is not supported on this system")
    private val _metalLayer: CAMetalLayer get() = layer as CAMetalLayer
    private var _skiaLayer: SkiaLayer? = null
    private lateinit var _redrawer: MetalRedrawer

    init {
        skikoInitializeUIView()
        isOpaque = false // For UIKit interop through a "Hole"

        _metalLayer.also {
            it.device = _device
            it.pixelFormat = MTLPixelFormat.BGRA8Unorm
            it.backgroundColor = CGColor.create(CGColorSpace.createDeviceRGB(), doubleArrayOf(0.0, 0.0, 0.0, 0.0))
            it.isFramebufferOnly = false
        }
    }
    constructor(
        skiaLayer: SkiaLayer,
        frame: CGRect = CGRect.Null()
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

        val scaledSize = CGSize(
            bounds.width * contentScaleFactor,
            bounds.height * contentScaleFactor
        )

        _metalLayer.drawableSize = scaledSize
    }
}

private fun UIView.skikoInitializeUIView() {
    isMultipleTouchEnabled = true
    isUserInteractionEnabled = true
}
