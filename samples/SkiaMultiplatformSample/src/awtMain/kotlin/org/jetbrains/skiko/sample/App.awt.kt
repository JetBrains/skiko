package org.jetbrains.skiko.sample

import org.jetbrains.skiko.*
import org.jetbrains.skiko.windows.*
import java.awt.Dimension
import javax.swing.*

fun main(args: Array<String>) {
    val skiaLayer = SkiaLayer()
    parseArgs(args)
    val app = run {
        //EmojiStory()
        AwtClocks(skiaLayer)
    }
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, app)
    SwingUtilities.invokeLater {
        val window = JFrame("Skiko example").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(800, 600)
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
        createWindowsJumpList()
    }
}

/**
 * The Jump List sample requires a separate executable for two reasons:
 * 1. The java.exe process somehow blocks the Jump Lists so to enable them we would have to provide
 *    an AppUserModelID for the sample's window, and
 * 2. In the current implementation, the Jump List items launch the calling process when clicked with
 *    command line arguments provided to the JumpListItem. Without a separate executable,
 *    java.exe will be launched.
 */
private fun createWindowsJumpList() {
    // Check if Jump Lists are supported
    if (JumpList.isSupported()) {
        // Start a Jump List building transaction
        JumpList.build {
            // If the process has an explicit AppUserModelID, it should be set for the Jump List
            // before calling [beginList]
            // setAppID("org.jetbrains.skiko.SkiaMultiplatformSample.awt")

            // Create a new Jump List
            beginList()

            // Collect elements to add to the Jump List, e.g. some recent documents
            val recentItems = listOf(
                JumpListItem("Recent item 1", "path\\to\\recent\\item\\1").apply {
                    attributes = JumpListItemAttributes().apply {
                        description = "The first recent item"
                        icon = JumpListItemIcon("explorer.exe", 0)
                    }
                }
            )

            // TODO: Exclude [removedItems] from [recentItems]
            val removedItems = getRemovedItems()

            // Add recent items to the Jump List
            addCategory("Recent items", recentItems)

            // Add a user task to the Jump List
            addUserTask(JumpListItem("User Task 1", "--user-task-1"))

            // Commit the Jump List and finish the transaction
            runCatching {
                commit()
            }.onFailure { e ->
                println("Couldn't build the Jump List. Please make sure Jump Lists are enabled for the user, " +
                        "and that the sample is run via a separate executable.")
                println(e.message)
            }
        }
    }
}

// When a Jump List item is clicked, the app is re-launched with an argument
// passed to the JumpListItem's constructor
private fun parseArgs(args: Array<String>) {
    for(arg in args) {
        when (arg) {
            "--user-task-1" -> println("User Task 1 in the Jump List was clicked!")
            "path\\to\\recent\\item\\1" -> println("Recent Item 1 in the Jump List was clicked!")
            else -> println("Unknown argument: ($arg)")
        }
    }
}
