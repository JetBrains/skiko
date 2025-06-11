import org.gradle.api.Project

val Project.wasmImports
    get() = layout.buildDirectory.asFile.get().resolve("imports")


val Project.skikoTestMjs
    get() = wasmImports.resolve("skiko-test.mjs")

const val IMPORT_GENERATOR = "import-generator"