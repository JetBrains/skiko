package org.jetbrains.skiko

import kotlinx.browser.window

actual val hostOs: OS by lazy {
    detectHostOs()
}

actual val hostArch: Arch = Arch.JS

actual val hostId by lazy {
    "${hostOs.id}-${hostArch.id}"
}

actual val kotlinBackend: KotlinBackend
    get() = KotlinBackend.JS

/**
 * A string identifying the platform on which the user's browser is running; for example:
 * "MacIntel", "Win32", "Linux x86_64", "Linux x86_64".
 * See https://developer.mozilla.org/en-US/docs/Web/API/Navigator/platform - deprecated
 *
 * A string containing the platform brand. For example, "Windows".
 * See https://developer.mozilla.org/en-US/docs/Web/API/NavigatorUAData/platform - new API,
 * but not supported in all browsers
 */
private fun getNavigatorInfo(): String =
    js("navigator.userAgentData ? navigator.userAgentData.platform : navigator.platform") as String


/**
 * In a browser, user platform can be obtained from different places:
 * - we attempt to use not-deprecated but experimental option first (not available in all browsers)
 * - then we attempt to use a deprecated option
 * - if both above return an empty string, we attempt to get `Platform` from `userAgent`
 *
 * Note: a client can spoof these values, so it's okay only for non-critical use cases.
 */
internal fun detectHostOs(): OS {
    val platformInfo = getNavigatorInfo().takeIf {
        it.isNotEmpty()
    } ?: window.navigator.userAgent

    return when {
        platformInfo.contains("Android", true) -> OS.Android
        platformInfo.contains("iPhone", true) -> OS.Ios
        platformInfo.contains("iOS", true) -> OS.Ios
        platformInfo.contains("iPad", true) -> OS.Ios
        platformInfo.contains("Linux", true) -> OS.Linux
        platformInfo.contains("Mac", true) -> OS.MacOS
        platformInfo.contains("Win", true) -> OS.Windows
        else -> OS.JS
    }
}