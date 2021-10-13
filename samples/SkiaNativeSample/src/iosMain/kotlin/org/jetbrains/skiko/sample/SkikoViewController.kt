package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.SkiaLayer
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

        val layer = SkiaLayer(width.toInt(), height.toInt())

        layer.initLayer()

        layer.view = UIView().apply {
            backgroundColor = UIColor.lightGrayColor
            view.addSubview(this)
            translatesAutoresizingMaskIntoConstraints = false
            leadingAnchor.constraintEqualToAnchor(view.leadingAnchor).active = true
            topAnchor.constraintEqualToAnchor(view.topAnchor).active = true
            widthAnchor.constraintEqualToAnchor(view.widthAnchor).active = true
            heightAnchor.constraintEqualToAnchor(view.heightAnchor).active = true
        }
    }
}
