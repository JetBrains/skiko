package org.jetbrains.skiko


actual interface SkikoInput {
    fun onInputEvent(event: SkikoInputEvent)

    actual object Empty : SkikoInput {
        override fun onInputEvent(event: SkikoInputEvent) = Unit
    }
}
