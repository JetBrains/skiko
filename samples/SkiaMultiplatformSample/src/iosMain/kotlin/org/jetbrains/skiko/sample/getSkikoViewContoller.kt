package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.sample.*
import platform.Foundation.*
import platform.UIKit.*

fun makeApp(skiaLayer: SkiaLayer) = IosClocks(skiaLayer)

fun getSkikoViewContoller(): UIViewController {
    val view = SkikoUIView(
        SkiaLayer().apply {
            gesturesToListen = SkikoGestureEventKind.values()
            skikoView = GenericSkikoView(this, makeApp(this))
        }
    )
    //view.currentKeyboardType = UIKeyboardTypePhonePad
    //view.currentReturnKeyType = UIReturnKeyType.UIReturnKeyDone
    return SkikoViewController(view)
}
