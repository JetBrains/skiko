package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.sample.*
import platform.Foundation.*
import platform.UIKit.*

fun makeApp(skiaLayer: SkiaLayer) = Clocks(skiaLayer)

fun getSkikoViewContoller(): UIViewController = SkikoViewController(
    SkikoUIView(
        SkiaLayer().apply {
            gesturesToListen = SkikoGestureEventKind.values()
            skikoView = GenericSkikoView(this, makeApp(this))
        }
    )
)
