package org.jetbrains.skiko.awt.font

import kotlinx.coroutines.runBlocking

private fun Iterable<String>.ignoreVirtualAwtFontFamilies() =
    filterNot { FontFamilyKey(it) in FontFamilyKey.Awt.awtLogicalFonts }

private fun Iterable<String>.ignoreEmbeddedFontFamilies(): List<String> {
    val embeddedFamilyKeys = runBlocking { JvmEmbeddedFontProvider.familyNames() }
        .map { FontFamilyKey(it) }
    val embeddedMappedFamilyKeys = runBlocking { JvmEmbeddedFontProvider.embeddedFontFamilyMap() }
        .map { FontFamilyKey(it.value) }

    return filterNot { FontFamilyKey(it) in embeddedFamilyKeys }
        .filterNot { FontFamilyKey(it) in embeddedMappedFamilyKeys }
}

private fun Iterable<String>.onlyAwtLogicalFamilies() =
    filter { FontFamilyKey(it) in FontFamilyKey.Awt.awtLogicalFonts }
