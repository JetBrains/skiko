package org.jetbrains.skiko

// No look and feel on iOS; only reachable if the `skiko.rendering.laf.global`
// system property is explicitly set, so a no-op is safe.
actual fun setSystemLookAndFeel(): Unit = Unit

// The jvmMain rendering pipeline (RenderFactory/Redrawer) is AWT-oriented and is not
// used by the RoboVM flavor: rendering goes through SkikoUIView + MetalRedrawer,
// like in the Kotlin/Native iOS flavor. RenderFactory.Default is never touched here.
internal actual fun makeDefaultRenderFactory(): RenderFactory =
    RenderFactory { _, _, _, _ ->
        throw UnsupportedOperationException(
            "RenderFactory is not used on RoboVM/iOS; use SkikoUIView instead"
        )
    }
