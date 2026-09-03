import java.io.File

fun findBenchmarkProjectDir(): File {
    val current = File(".").canonicalFile
    if (current.resolve("build.gradle.kts").exists() && current.name == "SkikoBenchmarks") {
        return current
    }

    val fromRepoRoot = current.resolve("benchmarks/SkikoBenchmarks")
    if (fromRepoRoot.resolve("build.gradle.kts").exists()) {
        return fromRepoRoot.canonicalFile
    }

    val fromScriptParent = File("benchmarks/SkikoBenchmarks").canonicalFile
    if (fromScriptParent.resolve("build.gradle.kts").exists()) {
        return fromScriptParent
    }

    throw IllegalStateException("Cannot locate benchmarks/SkikoBenchmarks project")
}
