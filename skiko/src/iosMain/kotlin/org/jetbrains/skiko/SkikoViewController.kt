package org.jetbrains.skiko

import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSCoder
import platform.UIKit.UIEvent
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController
import platform.UIKit.setFrame

@ExportObjCClass
class SkikoViewController : UIViewController {
    @OverrideInit
    constructor() : super(nibName = null, bundle = null)

    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        println("touchesBegan: $withEvent")
        super.touchesBegan(touches, withEvent)
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        println("touchesEnded $withEvent")
        super.touchesEnded(touches, withEvent)
    }

    internal lateinit var appFactory: (SkiaLayer) -> SkikoView
    fun setAppFactory(appFactory: (SkiaLayer) -> SkikoView) {
        this.appFactory = appFactory
    }

    override fun viewDidLoad() {
        super.viewDidLoad()

        val (width, height) = UIScreen.mainScreen.bounds.useContents {
            this.size.width to this.size.height
        }
        val layer = SkiaLayer().apply {
            skikoView = appFactory(this)
        }
        view.setFrame(CGRectMake(0.0, 0.0, width, height))
        layer.attachTo(this.view)
    }
}
