import java.io.File

sealed class ToolMode {
    class Incremental(
        val removedFiles: Iterable<File>,
        val modifiedFiles: Iterable<File>,
        val newFiles: Iterable<File>
    ) : ToolMode() {
        fun newOrModifiedFiles() = modifiedFiles + newFiles
        fun outdatedFiles() = modifiedFiles + removedFiles
    }
    class NonIncremental(val reason: String) : ToolMode()
}