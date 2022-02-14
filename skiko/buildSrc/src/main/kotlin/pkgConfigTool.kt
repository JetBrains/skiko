import java.io.File
import java.util.concurrent.TimeUnit
import org.gradle.api.GradleException

fun runPkgConfig(
    vararg packageNames: String,
): List<File> {
    val process = ProcessBuilder(
        "pkg-config", "--cflags", *packageNames
    ).run {
        environment()["PKG_CONFIG_ALLOW_SYSTEM_LIBS"] = "1"
        start()
    }.also { it.waitFor(10, TimeUnit.SECONDS) }

    if (process.exitValue() != 0) {
        throw GradleException("Error executing pkg-config: ${process.errorStream.bufferedReader().readText()}")
    }

    return process.inputStream.bufferedReader().readText().split(" ").map { it.trim() }
        .map { it.removePrefix("-I") }
        .map(::File)
}