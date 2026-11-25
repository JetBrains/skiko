package org.jetbrains.skia

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

private val LANG by lazy {
    val locale = NSLocale.currentLocale
    val language = locale.languageCode ?: "en"
    val country = locale.countryCode
    if (country.isNullOrEmpty()) language else "$language-$country"
}

internal actual fun defaultLanguageTag(): String = LANG
