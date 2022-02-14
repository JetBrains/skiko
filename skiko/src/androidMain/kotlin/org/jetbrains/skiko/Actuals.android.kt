package org.jetbrains.skiko

import android.content.*
import android.content.ClipboardManager
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

internal actual fun URIHandler_openUri(uri: String) {
    defaultContext!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
}

internal actual fun ClipboardManager_setText(text: String) {
    val clipboard = defaultContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("", text)
    clipboard.setPrimaryClip(clip)
}

internal actual fun ClipboardManager_getText(): String? {
    val context = defaultContext!!
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip ?: return null
    return clip.getItemAt(0)?.text?.toString()
}