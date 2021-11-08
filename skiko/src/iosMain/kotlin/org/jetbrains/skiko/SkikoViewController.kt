package org.jetbrains.skiko

import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSCoder
import platform.UIKit.UIEvent
import platform.UIKit.UITouch
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.UIKit.setFrame
import platform.UIKit.contentScaleFactor

@ExportObjCClass
class SkikoViewController : UIViewController {
    @OverrideInit
    constructor() : super(nibName = null, bundle = null)

    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        val sender = (touches.elementAt(0) as UITouch)
        val (x, y) = sender.locationInView(null).useContents { x to y }
        skikoLayer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.PRESS,
                state = SkikoGestureEventState.PRESSED
            )
        )
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesEnded(touches, withEvent)
    }

    internal lateinit var appFactory: (SkiaLayer) -> SkikoView
    fun setAppFactory(appFactory: (SkiaLayer) -> SkikoView) {
        this.appFactory = appFactory
    }

    private lateinit var skikoLayer: SkiaLayer
    override fun viewDidLoad() {
        super.viewDidLoad()

        val (width, height) = UIScreen.mainScreen.bounds.useContents {
            this.size.width to this.size.height
        }
        skikoLayer = SkiaLayer().apply {
            skikoView = appFactory(this)
        }
        view.contentScaleFactor = UIScreen.mainScreen.scale
        view.setFrame(CGRectMake(0.0, 0.0, width, height))
        skikoLayer.attachTo(this.view)
    }

    // viewDidUnload() is deprecated and not called.
    override fun viewDidDisappear(animated: Boolean) {
        skikoLayer.detach()
    }
}
