package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.GenericRenderer
import org.jetbrains.skiko.SkiaLayer
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*
import platform.Foundation.NSCoder

@ExportObjCClass
class SkikoView: UIView {
    @OverrideInit
    constructor() : super(CGRectMake(0.0, 0.0, 0.0, 0.0))
    @OverrideInit
    constructor(coder: NSCoder) : super(coder)
    override fun canBecomeFirstResponder(): Boolean {
        println("canBecomeFirstResponder")
        return true
    }
}
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

    override fun pressesBegan(presses: Set<*>, withEvent: UIPressesEvent?) {
        println("press $withEvent")
        view.becomeFirstResponder()
        // view.endEditing(true)
        super.pressesBegan(presses, withEvent)
    }

    override fun loadView() {
        view = SkikoView()
    }

    override fun viewDidLoad() {
        super.viewDidLoad()

        val (width, height) = UIScreen.mainScreen.bounds.useContents {
            this.size.width to this.size.height
        }
        view.contentScaleFactor = UIScreen.mainScreen.scale
        view.setFrame(CGRectMake(0.0, 0.0, width, height))
        val layer = SkiaLayer(width.toFloat(), height.toFloat())
        layer.renderer = GenericRenderer(layer, makeApp())
        layer.initLayer(view)
    }
}
