package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.*
import platform.UIKit.*
import platform.Foundation.*
import org.jetbrains.skiko.sample.*

fun makeApp(skiaLayer: SkiaLayer) = Clocks(skiaLayer)

fun getSkikoViewContoller():UIViewController = SkikoViewController(
    SkikoUIView(
        SkiaLayer().apply {
            gesturesToListen = SkikoGestureEventKind.values()
            skikoView = GenericSkikoView(this, makeApp(this))
        }
    )
)
