package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.SkiaLayer
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

        val skikoView = UIView().apply {
            setFrame(CGRectMake(0.0, 0.0, width, height))
            backgroundColor = UIColor.blueColor
            view.addSubview(this@apply)
            translatesAutoresizingMaskIntoConstraints = false
            leadingAnchor.constraintEqualToAnchor(view.leadingAnchor).active = true
            topAnchor.constraintEqualToAnchor(view.topAnchor).active = true
            widthAnchor.constraintEqualToAnchor(view.widthAnchor).active = true
            heightAnchor.constraintEqualToAnchor(view.heightAnchor).active = true
        }

        val layer = SkiaLayer(width.toFloat(), height.toFloat())
        layer.initLayer(view)
    }
}
