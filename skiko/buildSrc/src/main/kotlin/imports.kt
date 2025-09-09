import org.gradle.api.Project

val Project.wasmImports
    get() = layout.buildDirectory.dir("imports").get().asFile

val Project.setupMjs
    get() = wasmImports.resolve("setup.mjs")

val Project.setupReexportMjs
    get() = wasmImports.resolve("js-reexport-symbols.mjs")

val Project.skikoTestMjs
    get() = wasmImports.resolve("skiko-test.mjs")

const val IMPORT_GENERATOR = "import-generator"