package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.sample.*
import platform.Foundation.*
import platform.UIKit.*

fun getSkikoViewContoller(): UIViewController = SkikoViewController(
    SkikoUIView(
        SkiaLayer().apply {
            gesturesToListen = SkikoGestureEventKind.values()
            skikoView = GenericSkikoView(this, makeApp(this))
        }
    )
)

fun makeApp(skiaLayer: SkiaLayer) = Clocks(skiaLayer)

@ExportObjCClass
class SkikoViewController : UIViewController {
    @OverrideInit
    constructor() : super(nibName = null, bundle = null)
    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    constructor(skikoUIView: SkikoUIView) : this() {
        this.skikoUIView = skikoUIView
    }

    private var skikoUIView: SkikoUIView? = null

    override fun loadView() {
        if (skikoUIView == null) {
            super.loadView()
        } else {
            this.view = skikoUIView!!.load()
        }
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        skikoUIView?.showScreenKeyboard()
    }

    // viewDidUnload() is deprecated and not called.
    override fun viewDidDisappear(animated: Boolean) {
        skikoUIView?.detach()
    }
}
