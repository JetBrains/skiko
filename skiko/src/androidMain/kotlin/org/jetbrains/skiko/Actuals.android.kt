package org.jetbrains.skiko

import android.content.*
import android.content.ClipboardManager
import android.content.res.Configuration
import android.net.Uri
import android.view.PointerIcon.*
import android.view.View
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
            analytics: SkiaLayerAnalytics,
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

actual typealias Cursor = android.view.PointerIcon

// TODO: not sure if correct.
internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    if (component is View) {
        component.pointerIcon = cursor
    }
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? =
    if (component is View) {
        component.pointerIcon
    } else {
        null
    }

internal actual fun getCursorById(id: PredefinedCursorsId): Cursor =
    when (id) {
        PredefinedCursorsId.DEFAULT -> getSystemIcon(defaultContext!!, TYPE_DEFAULT)
        PredefinedCursorsId.CROSSHAIR -> getSystemIcon(defaultContext!!, TYPE_CROSSHAIR)
        PredefinedCursorsId.HAND -> getSystemIcon(defaultContext!!, TYPE_HAND)
        PredefinedCursorsId.TEXT -> getSystemIcon(defaultContext!!, TYPE_TEXT)
    }