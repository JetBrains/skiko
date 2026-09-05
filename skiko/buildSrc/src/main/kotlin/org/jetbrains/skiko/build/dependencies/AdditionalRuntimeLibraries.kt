package org.jetbrains.skiko.build.dependencies

import org.gradle.api.Project
import org.jetbrains.skiko.build.context.supportAwt
import org.jetbrains.skiko.build.utils.Arch
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoProperties

fun Project.registerAdditionalLibraries(
    targetOs: OS,
    targetArch: Arch,
    skikoProperties: SkikoProperties,
    artifacts: SkikoArtifacts,
): List<AdditionalRuntimeLibrary> {
    val angleTag = property("dependencies.angle") as String
    return listOfNotNull(
        if (supportAwt && targetOs == OS.Windows) {
            registerAdditionalRuntimeLibrary(
                targetOs = targetOs,
                targetArch = targetArch,
                skikoProperties = skikoProperties,
                artifacts = artifacts,
                name = "angle",
                archiveUrl = "https://github.com/JetBrains/angle-pack/releases/download/$angleTag/Angle-$angleTag-${targetOs.id}-Release-${targetArch.id}.zip",
                filesToInclude = listOf(
                    "out/Release-${targetOs.id}-${targetArch.id}/libEGL.dll",
                    "out/Release-${targetOs.id}-${targetArch.id}/libGLESv2.dll"
                ),
            )
        } else {
            null
        }
    )
}
