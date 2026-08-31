package org.jetbrains.skiko.build.utils

import java.io.File
import org.gradle.workers.WorkParameters

internal interface RunExternalProcessWorkParameters : WorkParameters {
    var workId: String
    var executable: String
    var workingDir: File
    var args: List<String>
}
