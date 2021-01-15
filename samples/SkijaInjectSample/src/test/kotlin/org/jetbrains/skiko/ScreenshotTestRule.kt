package org.jetbrains.skiko

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

// TODO macOs has wrong colors. Only white, black, red and green are correct
class ScreenshotTestRule(private val robot: Robot) : TestRule {
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

    fun assert(rectangle: Rectangle, id: String = "") {
        val actual = robot.createScreenCapture(rectangle)
        val name = if (id.isNotEmpty()) "${testIdentifier}_$id" else testIdentifier
        val actualFile = File(screenshotsDir, "${name}_actual.png")
        val expectedFile = File(screenshotsDir, "$name.png")
        if (actualFile.exists()) {
            actualFile.delete()
        }
        if (expectedFile.exists()) {
            val expected = ImageIO.read(expectedFile)
            if (!isContentSame(expected, actual)) {
                ImageIO.write(actual, "png", actualFile)
                throw AssertionError(
                    "Image mismatch! Expected image ${expectedFile.absolutePath}, actual: ${actualFile.absolutePath}"
                )
            }
        } else {
            ImageIO.write(actual, "png", actualFile)
            throw AssertionError(
                "Missing screenshot image " +
                        "${actualFile.absolutePath}. " +
                        "Did you mean to check in a new image?"
            )
        }
    }
}