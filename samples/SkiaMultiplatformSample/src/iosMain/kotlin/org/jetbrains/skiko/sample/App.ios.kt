// Use `xcodegen` first, then `open ./SkikoSample.xcodeproj` and then Run button in XCode.
package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkikoView
import org.jetbrains.skiko.SkikoViewController
import platform.UIKit.*
import platform.Foundation.*

fun makeApp(): SkikoView = BouncingBalls()

fun main() {
    val args = emptyArray<String>()
    memScoped {
        val argc = args.size + 1
        val argv = (arrayOf("skikoApp") + args).map { it.cstr.ptr }.toCValues()
        autoreleasepool {
            UIApplicationMain(argc, argv, null, NSStringFromClass(SkikoAppDelegate))
        }
    }
}

class SkikoAppDelegate : UIResponder, UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    @ObjCObjectBase.OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    override fun application(application: UIApplication, didFinishLaunchingWithOptions: Map<Any?, *>?): Boolean {
        window = UIWindow(frame = UIScreen.mainScreen.bounds)
        window!!.rootViewController = SkikoViewController().apply {
            setAppFactory { layer ->
                GenericSkikoView(layer, makeApp()).also {
                    layer.skikoView = it
                }
            }
        }
        window!!.makeKeyAndVisible()
        return true
    }
}
