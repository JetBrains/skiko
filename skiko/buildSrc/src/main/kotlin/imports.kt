import org.gradle.api.Project

val Project.wasmImports
    get() = layout.buildDirectory.dir("imports").get().asFile

fun Project.wasmImport(name: String) = wasmImports.resolve(name)

const val IMPORT_GENERATOR = "import-generator"