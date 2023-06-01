@file:Suppress("PrivatePropertyName") // Reflection-based properties have more meaningful names

package org.jetbrains.skiko

import org.jetbrains.skiko.ReflectionUtil.findFieldInHierarchy
import org.jetbrains.skiko.ReflectionUtil.getDeclaredMethodOrNull
import org.jetbrains.skiko.ReflectionUtil.getFieldValueOrNull
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@InternalSkikoApi
object AwtFontUtils {

    init {
        InternalSunApiChecker.isSunFontApiAccessible()
    }

    private val FontManagerFactoryClass = Class.forName("sun.font.FontManagerFactory")

    private val FontManagerClass = Class.forName("sun.font.FontManager")
    private val Font2DClass = Class.forName("sun.font.Font2D")
    private val FileFontClass = Class.forName("sun.font.FileFont")
    private val CompositeFontClass = Class.forName("sun.font.CompositeFont")
    private val CFontClass = Class.forName("sun.font.CFont")

    // FontManagerFactory methods
    private val FontManagerFactory_getInstanceMethod =
        getDeclaredMethodOrNull(FontManagerFactoryClass, "getInstance")

    // FontManager methods and fields
    private val FontManager_findFont2DMethod = getDeclaredMethodOrNull(
        FontManagerClass,
        "findFont2D",
        String::class.java, // Font name
        Int::class.javaPrimitiveType!!, // Font style (e.g., Font.BOLD)
        Int::class.javaPrimitiveType!! // Fallback (one of the FontManager.*_FALLBACK values)
    )

    // Font2D methods and fields
    private val Font2D_getTypographicFamilyNameMethod =
        getFont2DMethodOrNull("getTypographicFamilyName")
    private val Font2D_getFamilyNameMethod =
        getFont2DMethodOrNull("getFamilyName", Locale::class.java)
    private val Font2D_handleField =
        findFieldInHierarchy(Font2DClass) { it.name == "handle" }

    // Font2DHandle fields
    private val Font2DHandle_font2DField =
        findFieldInHierarchy(Class.forName("sun.font.Font2DHandle")) {
            it.name == "font2D"
        }

    // FileFont methods
    private val FileFont_getPublicFileNameMethod =
        getDeclaredMethodOrNull(clazz = FileFontClass, name = "getPublicFileNameMethod")

    // CompositeFont methods
    private val CompositeFont_getSlotFontMethod =
        getDeclaredMethodOrNull(clazz = CompositeFontClass, name = "getSlotFont", Int::class.javaPrimitiveType!!)

    // Copy of FontManager.LOGICAL_FALLBACK
    private const val LOGICAL_FALLBACK = 2

    private val font2DHandlesCache = ConcurrentHashMap<Font, Any>()

    /**
     * Indicate whether the current JVM is able to resolve font properties
     * accurately or not.
     *
     * This value will be `true` if using the JetBrains Runtime. It will be
     * `false` otherwise, indicating that this class is not able to return
     * valid values.
     *
     * If the return value is `false`, you should assume all APIs in this class
     * will return `null` as we can't obtain the necessary information.
     *
     * On other JVMs running on Windows and Linux, the AWT implementation is
     * not enumerating font families correctly. E.g., you may have these entries
     * for JetBrains Mono, instead of a single entry: _JetBrains Mono, JetBrains
     * Mono Bold, JetBrains Mono ExtraBold, JetBrains Mono ExtraLight, JetBrains
     * Mono Light, JetBrains Mono Medium, JetBrains Mono SemiBold, JetBrains
     * Mono Thin_.
     *
     * On the JetBrains Runtime, there are additional APIs that provide the
     * necessary information needed to list the actual font families as single
     * entries, as one would expect.
     */
    val isAbleToResolveFontProperties: Boolean
        get() = Font2D_getTypographicFamilyNameMethod != null

    /**
     * Try to resolve a font family name, that could be a logical font
     * face (e.g., [Font.DIALOG]), to the actual physical font family
     * it is an alias for.
     *
     * @param fontName The name of the font face
     * @param style The desired font name; must be one of the styles
     * supported by the AWT [Font]. It is [Font.PLAIN] by default.
     *
     * @return The resolved physical font name, or `null` if it can't
     * be resolved (either it's unknown, or [isAbleToResolveFontProperties]
     * is false)
     *
     * @see isAbleToResolveFontProperties
     */
    internal fun resolvePhysicalFontNameOrNull(
        fontName: String,
        style: Int = Font.PLAIN
    ): String? {
        if (!isAbleToResolveFontProperties) return null

        val fontManager = awtFontManager()
        val font2D =
            checkNotNull(FontManager_findFont2DMethod) { "FontManager#findFont2DMethod() is not available" }
                .invoke(fontManager, fontName, style, LOGICAL_FALLBACK)

        return when {
            CompositeFontClass.isInstance(font2D) -> {
                // For Windows and Linux
                val physicalFontObject = CompositeFont_getSlotFontMethod?.invoke(font2D, 0)
                Font2D_getFamilyNameMethod?.invoke(physicalFontObject, Locale.getDefault()) as String?
            }

            CFontClass.isInstance(font2D) -> {
                // For macOS
                val clazz = Class.forName("sun.font.CFont")
                getFieldValueOrNull(clazz, font2D, String::class.java, "nativeFontName")
            }

            else -> error("Unsupported Font2D subclass: ${font2D.javaClass.name}")
        }
    }

    /**
     * The list of font family names available through AWT. This list should
     * be used instead of the one provided by [GraphicsEnvironment.getAvailableFontFamilyNames]
     * since it will provide consistent results across platforms.
     *
     * Will return `null` if [isAbleToResolveFontProperties] is `false`.
     *
     * See [fontFamilyName] for further details.
     *
     * @see fontFamilyName
     * @see isAbleToResolveFontProperties
     */
    fun fontFamilyNamesOrNull(
        graphicsEnvironment: GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    ): SortedSet<String>? {
        if (!isAbleToResolveFontProperties) return null

        return graphicsEnvironment.allFonts.map { font -> font.fontFamilyName!! }
            .toSortedSet()
    }

    /**
     * The preferred font family name, which should be used instead of the
     * [Font.getFamily] and `Font2D.familyName`. It will be `null` if
     * [isAbleToResolveFontProperties] is `false`.
     *
     * On Windows, and potentially in other cases, the family name as reported
     * by AWT can contain the style and weight of the [Font] in addition to the
     * _actual_ font family name. This can cause issues when trying to match up
     * AWT fonts with Skia typefaces, and if used for listing font families,
     * will result in multiple entries being present in the list.
     *
     * You can use [fontFamilyNamesOrNull] to enumerate the actual family names
     * available via AWT.
     *
     * @see fontFamilyNamesOrNull
     * @see isAbleToResolveFontProperties
     */
    val Font.fontFamilyName: String?
        get() {
            if (!isAbleToResolveFontProperties) return null

            val font2D = obtainFont2D()
            return checkNotNull(Font2D_getTypographicFamilyNameMethod) { "Font2D#getTypographicFamilyName() is not available" }
                .invoke(font2D) as String
        }

    /**
     * The file that a [Font] is loaded from; it will be `null`
     * if [isAbleToResolveFontProperties] is `false`, or if the
     * font is not backed by a [sun.font.FileFont].
     *
     * @see fontFamilyNamesOrNull
     * @see isAbleToResolveFontProperties
     */
    val Font.fontFileName: String?
        get() {
            if (!isAbleToResolveFontProperties) return null

            val font2D = obtainFont2D()
            if (!FileFontClass.isInstance(font2D)) return null

            return checkNotNull(FileFont_getPublicFileNameMethod) { "FileFont#getPublicFileName() is not available" }
                .invoke(font2D) as String
        }

    private fun Font.obtainFont2D(): Any {
        // Don't store the Font2D instance directly, in case the handle may be changed
        // later on. Logic adopted from java.awt.Font#getFont2D()
        val handle = font2DHandlesCache.getOrPut(this) {
            val fontManager = awtFontManager()
            val font2D =
                checkNotNull(FontManager_findFont2DMethod) { "FontManager#findFont2DMethod() is not available" }
                    .invoke(fontManager, name, style, LOGICAL_FALLBACK)
            checkNotNull(Font2D_handleField) { "Font2D#handle is not available" }
                .get(font2D)
        }

        return checkNotNull(Font2DHandle_font2DField) { "Font2DHandle#font2D is not available" }
            .get(handle)
    }

    private fun awtFontManager() =
        checkNotNull(FontManagerFactory_getInstanceMethod) { "FontManagerFactory#getInstanceMethod() not available" }
            .invoke(null)

    private fun getFont2DMethodOrNull(methodName: String, vararg parameters: Class<*>): Method? =
        getDeclaredMethodOrNull(Font2DClass, methodName, *parameters)
}
