// Use `xcodegen` first, then `open ./SkikoSample.xcodeproj` and then Rub

package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skiko.*

fun main(args: Array<String>) {
    val paint = Paint().apply { color = Color.GREEN }
    println("Paint is $paint")
    runSkikoMain()
}

