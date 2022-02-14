package org.jetbrains.skiko

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import org.jetbrains.skiko.redrawer.Redrawer

actual fun setSystemLookAndFeel(): Unit = TODO()

internal class AndroidOpenGLRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : Redrawer {
    override fun dispose() = TODO()
    override fun needRedraw() = TODO()
    override fun redrawImmediately() = TODO()

    override val renderInfo: String get() = "Android renderer"
}

internal actual fun makeDefaultRenderFactory(): RenderFactory {
    return object : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            properties: SkiaLayerProperties
        ): Redrawer = when (hostOs) {
            OS.Android -> AndroidOpenGLRedrawer(layer, properties)
            else -> throw IllegalArgumentException("Must not happen")
        }
    }
}

private var defaultContext: Context? = null

internal fun initDefaultContext(context: Context) {
    defaultContext = context
}

actual val currentSystemTheme: SystemTheme
    get() {
        if (defaultContext == null) return SystemTheme.UNKNOWN
        return when (defaultContext!!.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> SystemTheme.DARK
            Configuration.UI_MODE_NIGHT_NO -> SystemTheme.LIGHT
            else -> SystemTheme.UNKNOWN
        }
    }

actual fun openUri(uri: String) {
    defaultContext!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
}