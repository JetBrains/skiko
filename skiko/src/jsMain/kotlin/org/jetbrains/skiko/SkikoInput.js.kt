package org.jetbrains.skiko

actual interface SkikoInput {
    fun onInputEvent(event: SkikoInputEvent)
}
