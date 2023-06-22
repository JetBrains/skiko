package org.jetbrains.skiko.awt.font

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FontFamilyKeyTest {

    @Test
    fun `should be equals to another instance with the same family name (case sensitive)`() {
        assertEquals(FontFamilyKey("Banana"), FontFamilyKey("Banana"))
    }

    @Test
    fun `should be equals to another instance with the same family name (case insensitive)`() {
        assertEquals(FontFamilyKey("Banana"), FontFamilyKey("banana"))
    }

    @Test
    fun `should not be equals to another instance with a different family name`() {
        assertNotEquals(FontFamilyKey("Banana"), FontFamilyKey("potato"))
    }

    @Test
    fun `should have the same hashcode as another instance with the same family name (case sensitive)`() {
        assertEquals(FontFamilyKey("Banana").hashCode(), FontFamilyKey("Banana").hashCode())
    }

    @Test
    fun `should have the same hashcode as another instance with the same family name (case insensitive)`() {
        assertEquals(FontFamilyKey("Banana").hashCode(), FontFamilyKey("banana").hashCode())
    }

    @Test
    fun `should not have the same hashcode as another instance with a different family name`() {
        assertNotEquals(FontFamilyKey("Banana").hashCode(), FontFamilyKey("potato").hashCode())
    }
}