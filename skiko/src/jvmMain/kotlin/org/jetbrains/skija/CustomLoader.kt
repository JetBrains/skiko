package org.jetbrains.skija

class CustomLoader : Runnable {
    override fun run() {
        org.jetbrains.skiko.Library.load()
    }
}