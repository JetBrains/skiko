package org.jetbrains.skiko

@OptIn(kotlin.ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
actual interface SkikoInput {
    fun onInputEvent(event: SkikoInputEvent)

    actual object Empty : SkikoInput {
        override fun onInputEvent(event: SkikoInputEvent) = Unit
    }
}
