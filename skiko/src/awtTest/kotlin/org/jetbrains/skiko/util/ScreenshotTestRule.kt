package org.jetbrains.skiko.util

import org.jetbrains.skia.Image
import org.jetbrains.skiko.toImage
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.awt.Rectangle
import java.awt.Robot
import java.io.File

// WARNING!!!
// macOS has wrong colors ([128, 128, 128] isn't [128, 128, 128] on screenshot). Only white, black, red and green are correct.
// So use only these color for cross-platform screenshots tests.
// TODO fix colors on macOS
class ScreenshotTestRule : TestRule {
    private val robot by lazy { Robot() }

    private lateinit var testIdentifier: String
    private val screenshotsDir = File(System.getProperty("skiko.test.screenshots.dir")!!)

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                testIdentifier = "${description.className}_${description.methodName}"
                    .replace(".", "_")
                    .replace(",", "_")
                    .replace(" ", "_")
                    .replace("(", "_")
                    .replace(")", "_")
                    .replace("__", "_")
                    .replace("__", "_")
                    .removePrefix("_")
                    .removeSuffix("_")
                base.evaluate()
            }
        }
    }

    fun assert(
        rectangle: Rectangle,
        id: String = "",
        testIdentifier: String = this.testIdentifier
    ) {
        val actual = robot.createScreenCapture(rectangle)
        assert(actual.toImage(), id, testIdentifier)
    }

    fun assert(
        actual: Image,
        id: String = "",
        testIdentifier: String = this.testIdentifier
    ) {
        val name = if (id.isNotEmpty()) "${testIdentifier}_$id" else testIdentifier
        val actualFile = File(screenshotsDir, "${name}_actual.png")
        val expectedFile = File(screenshotsDir, "$name.png")
        if (actualFile.exists()) {
            actualFile.delete()
        }
        if (expectedFile.exists()) {
            val expected = Image.makeFromEncoded(expectedFile.readBytes())
            // macOs screenshots can have different color on different configurations
            if (!isContentSame(expected, actual, sensitivity = 0.25)) {
                actualFile.writeBytes(actual.encodeToData()!!.bytes)
                throw AssertionError(
                    "Image mismatch! Expected image ${expectedFile.absolutePath}, actual: ${actualFile.absolutePath}"
                )
            }
        } else {
            actualFile.writeBytes(actual.encodeToData()!!.bytes)
            throw AssertionError(
                "Missing screenshot image " +
                        "${actualFile.absolutePath}. " +
                        "Did you mean to check in a new image?"
            )
        }
    }
}