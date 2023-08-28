package org.jetbrains.skiko

import org.junit.Assume.assumeTrue
import java.util.*
import javax.accessibility.Accessible
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.accessibility.AccessibleStateSet
import kotlin.test.Test

class AccessibilityKtTest {
    private companion object {
        init {
            Library.load()
        }
    }

    @Test
    fun `getCAccessible is available`() {
        assumeTrue(hostOs == OS.MacOS)

        val context = object : AccessibleContext() {
            override fun getAccessibleRole(): AccessibleRole {
                return AccessibleRole.PANEL
            }

            override fun getAccessibleStateSet(): AccessibleStateSet {
                return AccessibleStateSet()
            }

            override fun getAccessibleIndexInParent(): Int {
                return 0
            }

            override fun getAccessibleChildrenCount(): Int {
                return 0
            }

            override fun getAccessibleChild(i: Int): Accessible? {
                return null
            }

            override fun getLocale(): Locale {
                return Locale.getDefault()
            }

        }
        val accessible = Accessible {
            context
        }

        initializeCAccessible(accessible)
    }
}