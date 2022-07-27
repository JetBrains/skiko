package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.*

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
