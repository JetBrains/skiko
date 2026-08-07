package org.jetbrains.skiko

import org.robovm.apple.foundation.NSBundle
import org.robovm.apple.uikit.UIViewController
import org.robovm.objc.annotation.CustomClass

/**
 * Convenience [UIViewController] that hosts a [SkikoUIView],
 * mirroring the API of the Kotlin/Native iOS Skiko flavor.
 */
@CustomClass("SkikoViewController")
open class SkikoViewController(private val viewBuilder: () -> SkikoUIView) : UIViewController(null as String?, null as NSBundle?) {

    override fun loadView() {
        view = viewBuilder()
    }
}
