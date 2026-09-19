@file:OptIn(kotlinx.cinterop.BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko.sample.extensions

import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import platform.Foundation.NSStringFromClass
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.UIKit.UIApplicationDelegateProtocolMeta
import platform.UIKit.UIApplicationMain
import platform.UIKit.UIResponder
import platform.UIKit.UIResponderMeta
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private lateinit var skottiePlayer: SkottieAnimationPlayer

fun main() {
    skottiePlayer = loadSkottieAnimationPlayer()
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

    @OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    override fun application(
        application: UIApplication,
        didFinishLaunchingWithOptions: Map<Any?, *>?
    ): Boolean {
        val skiaLayer = SkiaLayer()
        skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer) { canvas, width, height, _ ->
            skottiePlayer.render(canvas, width, height)
        }
        val animation = IosSkottieAnimation(skiaLayer)

        dispatch_async(dispatch_get_main_queue()) {
            skiaLayer.needRender()
        }

        window = UIWindow(frame = UIScreen.mainScreen.bounds).also {
            it.rootViewController = animation.viewController
            it.makeKeyAndVisible()
        }
        return true
    }
}
