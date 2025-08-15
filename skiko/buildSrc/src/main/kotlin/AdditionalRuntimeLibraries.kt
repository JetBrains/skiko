import org.gradle.api.Project

fun Project.registerAdditionalLibraries(
    targetOs: OS,
    targetArch: Arch,
    skikoProperties: SkikoProperties
): List<AdditionalRuntimeLibrary> {
    val angleTag = property("dependencies.angle") as String
    return listOfNotNull(
        if (supportAwt && targetOs == OS.Windows) {
            registerAdditionalRuntimeLibrary(
                targetOs = targetOs,
                targetArch = targetArch,
                skikoProperties = skikoProperties,
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
