package org.jetbrains.skiko

import android.content.*
import android.content.res.Configuration
import android.view.View

actual fun setSystemLookAndFeel(): Unit = TODO()

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
