// Use `xcodegen` first, then `open ./SkikoSample.xcodeproj` and then Run button in XCode.
package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import kotlinx.cinterop.*
import platform.UIKit.*
import platform.Foundation.*

fun main() {
    val paint = Paint().apply { color = Color.GREEN }
    println("Paint is $paint")
    runSkikoMain()
}

fun runSkikoMain(args: Array<String> = emptyArray()) {
    memScoped {
        val argc = args.size + 1
        val argv = (arrayOf("skikoApp") + args).map { it.cstr.ptr }.toCValues()

        autoreleasepool {
            UIApplicationMain(argc, argv, null, NSStringFromClass(SkikoAppDelegate))
        }
    }
}

