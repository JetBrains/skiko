package me.user.shared

import kotlinx.cinterop.*
import org.jetbrains.skiko.*
import platform.UIKit.*
import platform.Foundation.*
import org.jetbrains.skiko.sample.*

fun makeApp(skiaLayer: SkiaLayer) = Clocks(skiaLayer)

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
        window!!.rootViewController = SkikoViewController(
            SkikoUIView(
                SkiaLayer().apply {
                    gesturesToListen = SkikoGestureEventKind.values()
                    skikoView = GenericSkikoView(this, makeApp(this))
                }
            )
        )
        window!!.makeKeyAndVisible()
        return true
    }
}
