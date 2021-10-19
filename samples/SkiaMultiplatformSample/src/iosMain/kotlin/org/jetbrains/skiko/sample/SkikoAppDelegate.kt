package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import platform.UIKit.*
import platform.Foundation.*
import platform.CoreGraphics.CGRectMake

class SkikoAppDelegate : UIResponder, UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    @ObjCObjectBase.OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    @ObjCAction
    fun rotated() {
        val (width, height) = UIScreen.mainScreen.bounds.useContents {
            this.size.width to this.size.height
        }
        window!!.rootViewController!!.view!!.setFrame(CGRectMake(0.0, 0.0, width, height))
        println("rotate: w:$width h:$height")
    }

    override fun application(application: UIApplication, didFinishLaunchingWithOptions: Map<Any?, *>?): Boolean {
        window = UIWindow(frame = UIScreen.mainScreen.bounds)
        window!!.rootViewController = SkikoViewController()
        window!!.makeKeyAndVisible()
        NSNotificationCenter.defaultCenter.addObserver(
            this,
            NSSelectorFromString("rotated"),
            UIDeviceOrientationDidChangeNotification,
            null
        )
        return true
    }
}