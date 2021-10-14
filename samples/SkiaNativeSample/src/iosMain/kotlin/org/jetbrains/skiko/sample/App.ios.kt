// Use `xcodegen` first, then `open ./SkikoSample.xcodeproj` and then Run button in XCode.
package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import platform.UIKit.*
import platform.Foundation.*

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

