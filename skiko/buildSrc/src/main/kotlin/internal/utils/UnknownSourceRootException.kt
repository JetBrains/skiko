package internal.utils

import java.io.File

internal class UnknownSourceRootException(
    sourceFile: File,
    sourceRoots: Collection<File>
) : IllegalStateException(
        buildString {
            appendLine("Could not find source root for: $sourceFile")
            appendLine("Known source roots:")
            for (root in sourceRoots) {
                appendLine("* $root")
            }
        }
    )