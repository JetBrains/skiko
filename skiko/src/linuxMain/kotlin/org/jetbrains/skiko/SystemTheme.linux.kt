@file:OptIn(ExperimentalForeignApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.fgets
import platform.posix.getenv
import platform.posix.pclose
import platform.posix.popen

actual val currentSystemTheme: SystemTheme
    get() = detectLinuxSystemTheme()

private fun detectLinuxSystemTheme(): SystemTheme {
    detectThemeFromGtkThemeEnvVar()?.let { return it }

    return when (detectLinuxDesktopEnvironment()) {
        LinuxDesktopEnvironment.KDE ->
            detectKdeTheme() ?: detectGnomeTheme() ?: detectGtkThemeFallback() ?: SystemTheme.UNKNOWN
        LinuxDesktopEnvironment.GNOME ->
            detectGnomeTheme() ?: detectGtkThemeFallback() ?: detectKdeTheme() ?: SystemTheme.UNKNOWN
        LinuxDesktopEnvironment.XFCE ->
            detectXfceTheme() ?: detectGtkThemeFallback() ?: detectGnomeTheme() ?: SystemTheme.UNKNOWN
        LinuxDesktopEnvironment.CINNAMON ->
            detectCinnamonTheme() ?: detectGtkThemeFallback() ?: detectGnomeTheme() ?: SystemTheme.UNKNOWN
        LinuxDesktopEnvironment.MATE ->
            detectMateTheme() ?: detectGtkThemeFallback() ?: detectGnomeTheme() ?: SystemTheme.UNKNOWN
        LinuxDesktopEnvironment.UNKNOWN ->
            detectGnomeTheme()
                ?: detectKdeTheme()
                ?: detectXfceTheme()
                ?: detectCinnamonTheme()
                ?: detectMateTheme()
                ?: detectGtkThemeFallback()
                ?: SystemTheme.UNKNOWN
    }
}

private enum class LinuxDesktopEnvironment {
    KDE,
    GNOME,
    XFCE,
    CINNAMON,
    MATE,
    UNKNOWN,
}

private fun detectLinuxDesktopEnvironment(): LinuxDesktopEnvironment {
    if (!getEnv("KDE_FULL_SESSION").isNullOrBlank()) return LinuxDesktopEnvironment.KDE

    val desktop =
        listOf(
            getEnv("XDG_CURRENT_DESKTOP"),
            getEnv("XDG_SESSION_DESKTOP"),
            getEnv("DESKTOP_SESSION"),
            getEnv("GDMSESSION"),
        )
            .filterNotNull()
            .joinToString(separator = ":")
            .lowercase()

    return when {
        desktop.contains("kde") || desktop.contains("plasma") -> LinuxDesktopEnvironment.KDE
        desktop.contains("gnome") || desktop.contains("unity") -> LinuxDesktopEnvironment.GNOME
        desktop.contains("xfce") || desktop.contains("xubuntu") -> LinuxDesktopEnvironment.XFCE
        desktop.contains("cinnamon") -> LinuxDesktopEnvironment.CINNAMON
        desktop.contains("mate") -> LinuxDesktopEnvironment.MATE
        else -> LinuxDesktopEnvironment.UNKNOWN
    }
}

private fun detectThemeFromGtkThemeEnvVar(): SystemTheme? {
    val gtkTheme = getEnv("GTK_THEME")?.trim().orEmpty()
    if (gtkTheme.isBlank()) return null

    val normalized = gtkTheme
        .lowercase()
        .trim()
        .trim('\'', '"')

    return when {
        normalized.endsWith(":dark") || normalized.contains("dark") -> SystemTheme.DARK
        normalized.endsWith(":light") || normalized.contains("light") -> SystemTheme.LIGHT
        else -> null
    }
}

private fun detectGnomeTheme(): SystemTheme? {
    detectGsettingsColorScheme(schema = "org.gnome.desktop.interface")?.let { return it }
    val theme = readGSettings(schema = "org.gnome.desktop.interface", key = "gtk-theme") ?: return null
    return detectFromThemeName(theme)
}

private fun detectCinnamonTheme(): SystemTheme? {
    val theme = readGSettings(schema = "org.cinnamon.desktop.interface", key = "gtk-theme") ?: return null
    return detectFromThemeName(theme)
}

private fun detectMateTheme(): SystemTheme? {
    val theme = readGSettings(schema = "org.mate.interface", key = "gtk-theme") ?: return null
    return detectFromThemeName(theme)
}

private fun detectXfceTheme(): SystemTheme? {
    val theme = readCommandOutput("xfconf-query -c xsettings -p /Net/ThemeName") ?: return null
    return detectFromThemeName(theme)
}

private fun detectKdeTheme(): SystemTheme? {
    val scheme = readKdeConfig(file = "kdeglobals", group = "General", key = "ColorScheme")
    detectFromThemeName(scheme)?.let { return it }

    val lookAndFeel = readKdeConfig(file = "kdeglobals", group = "KDE", key = "LookAndFeelPackage")
    detectFromThemeName(lookAndFeel)?.let { return it }

    val plasmaTheme = readKdeConfig(file = "plasmarc", group = "Theme", key = "name")
        ?: readKdeConfig(file = "plasmashellrc", group = "Theme", key = "name")
    detectFromThemeName(plasmaTheme)?.let { return it }

    return null
}

private fun detectGtkThemeFallback(): SystemTheme? {
    val theme = readGSettings(schema = "org.gnome.desktop.interface", key = "gtk-theme") ?: return null
    return detectFromThemeName(theme)
}

private fun detectFromThemeName(name: String?): SystemTheme? {
    if (name.isNullOrBlank()) return null

    val normalized = name
        .trim()
        .trim('\'', '"')
        .lowercase()

    return when {
        normalized.contains("twilight") -> SystemTheme.DARK
        normalized.contains("dark") -> SystemTheme.DARK
        normalized.contains("light") -> SystemTheme.LIGHT
        else -> null
    }
}

private fun detectGsettingsColorScheme(schema: String): SystemTheme? {
    val raw = readGSettings(schema = schema, key = "color-scheme") ?: return null
    return when (raw.lowercase()) {
        "prefer-dark" -> SystemTheme.DARK
        "default", "prefer-light" -> SystemTheme.LIGHT
        else -> null
    }
}

private fun readGSettings(schema: String, key: String): String? {
    return readCommandOutput("gsettings get $schema $key")?.trim('\'', '"')
}

private fun readKdeConfig(file: String, group: String, key: String): String? {
    val groupEscaped = shellEscapeSingleQuotes(group)
    val keyEscaped = shellEscapeSingleQuotes(key)
    val fileEscaped = shellEscapeSingleQuotes(file)
    val candidates = listOf(
        "kreadconfig6 --file '$fileEscaped' --group '$groupEscaped' --key '$keyEscaped'",
        "kreadconfig5 --file '$fileEscaped' --group '$groupEscaped' --key '$keyEscaped'",
    )
    for (cmd in candidates) {
        readCommandOutput(cmd)?.let { return it }
    }
    return null
}

private fun shellEscapeSingleQuotes(value: String): String = value.replace("'", "'\"'\"'")

private fun getEnv(name: String): String? = getenv(name)?.toKString()

private fun readCommandOutput(command: String): String? {
    val pipe = popen("$command 2>/dev/null", "r") ?: return null
    val output = StringBuilder()
    val exitCode = try {
        val buffer = ByteArray(4 * 1024)
        buffer.usePinned { pinned ->
            while (true) {
                val linePtr = fgets(pinned.addressOf(0), buffer.size, pipe) ?: break
                output.append(linePtr.toKString())
            }
        }
        pclose(pipe)
    } catch (_: Throwable) {
        pclose(pipe)
    }
    if (exitCode != 0) return null
    return output.toString().trim().takeIf { it.isNotEmpty() }
}

