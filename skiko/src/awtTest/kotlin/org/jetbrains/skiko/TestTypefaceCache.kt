package org.jetbrains.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile
import java.io.File

internal class TestTypefaceCache : TypefaceCache {

    val families = mutableMapOf<FontFamilyKey, FontFamily>()

    var lastGetName: String? = null
        private set

    override val size: Int
        get() = families.size

    override suspend fun addResource(
        resource: String,
        loader: ClassLoader
    ) {
        val res = loader.getResourceAsStream(resource)
            ?: ClassLoader.getSystemResourceAsStream(resource)
            ?: error("Unable to access the resources from the provided classloader")

        val resourceBytes = withContext(Dispatchers.IO) {
            res.readAllBytes()
        }
        val typeface = Typeface.makeFromData(Data.makeFromBytes(resourceBytes))
        addTypeface(typeface)
    }

    override fun addFile(file: File) {
        val typeface = Typeface.makeFromFile(file.absolutePath)
        addTypeface(typeface)
    }

    override fun addTypeface(typeface: Typeface) {
        val key = FontFamilyKey(typeface.familyName)
        val fontFamily = families.getOrPut(key) { FontFamily(typeface.familyName, FontFamily.FontFamilySource.Custom) }
        fontFamily += typeface
    }

    override fun removeFontFamily(familyName: String) {
        families -= FontFamilyKey(familyName)
    }

    override fun clear() {
        families.clear()
    }

    override fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        val family = getFontFamilyOrNull(familyName)
            ?: return null

        return family[fontStyle]
    }

    override fun getFontFamilyOrNull(familyName: String): FontFamily? {
        lastGetName = familyName
        return families[FontFamilyKey(familyName)]
    }

    override fun familyNames() =
        families.keys
            .map { it.familyName }
            .toSet()

    fun resetNamesTracking() {
        lastGetName = null
    }
}
