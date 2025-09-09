import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val target = "${targetOs}-${targetArch}"

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
  version = project.properties["skiko.version"] as String
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$version")
    implementation("org.jetbrains.runtime:jbr-api:1.5.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClass.set("SkiaAwtSample.AppKt")
}

val additionalArguments = mutableMapOf<String, String>()

val casualRun = tasks.named<JavaExec>("run") {
    systemProperty("skiko.fps.enabled", "true")
    systemProperty("skiko.linux.autodpi", "true")
    systemProperty("skiko.hardwareInfo.enabled", "true")
    systemProperty("skiko.win.exception.logger.enabled", "true")
    systemProperty("skiko.win.exception.handler.enabled", "true")
    jvmArgs("-ea")
    // jvmArgs("-Xcheck:jni")
    // Use systemProperty("skiko.library.path", "/tmp") to test loader.
    System.getProperties().entries
        .associate {
            (it.key as String) to (it.value as String)
        }
        .filterKeys { it.startsWith("skiko.") }
        .forEach { systemProperty(it.key, it.value) }
    additionalArguments.forEach { systemProperty(it.key, it.value) }
}

tasks.register("runSoftware") {
    additionalArguments += mapOf("skiko.renderApi" to "DIRECT_SOFTWARE")
    dependsOn(casualRun)
}

tasks.register("runWithTransparency") {
    additionalArguments += mapOf("skiko.transparency" to "true")
    dependsOn(casualRun)
}

tasks.register("runInterop") {
    additionalArguments += mapOf("skiko.swing.interop" to "true")
    dependsOn(casualRun)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}
