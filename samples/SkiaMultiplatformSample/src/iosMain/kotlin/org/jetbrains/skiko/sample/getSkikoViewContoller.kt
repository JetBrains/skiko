package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.sample.*
import platform.Foundation.*
import platform.UIKit.*

var skikoUiView: SkikoUIView? = null
fun makeApp(skiaLayer: SkiaLayer) = TextInput({ skikoUiView?.getText() ?: "empty" })

fun getSkikoViewContoller(): UIViewController = SkikoViewController(
    SkikoUIView(
        SkiaLayer().apply {
            gesturesToListen = SkikoGestureEventKind.values()
            skikoView = GenericSkikoView(this, makeApp(this))
        }
    ).also {
        skikoUiView = it
    }
)
