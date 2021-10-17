package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.GenericRenderer
import org.jetbrains.skiko.GenericSkikoApp
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoEventProcessor
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*
import platform.Foundation.NSCoder

@ExportObjCClass
class SkikoViewController : UIViewController {

    @OverrideInit
    constructor() : super(nibName = null, bundle = null)

    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    override fun viewDidLoad() {
        super.viewDidLoad()

        val (width, height) = UIScreen.mainScreen.bounds.useContents {
            this.size.width to this.size.height
        }
        view.setFrame(CGRectMake(0.0, 0.0, width, height))
        val layer = SkiaLayer(width.toFloat(), height.toFloat())
        layer.setApp(GenericSkikoApp(layer, makeApp()))
        layer.initLayer(view)
    }
}
