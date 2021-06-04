import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.1")
    implementation("org.jetbrains.skiko:skiko-jvm-runtime-$target:$version")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClass.set("SkijaInjectSample.AppKt")
}

val additionalArguments = mutableMapOf<String, String>()

val casualRun = tasks.named<JavaExec>("run") {
    systemProperty("skiko.fps.enabled", "true")
    systemProperty("skiko.hardwareInfo.enabled", "true")
    jvmArgs?.add("-ea")
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
    additionalArguments += mapOf("skiko.renderApi" to "SOFTWARE")
    dependsOn(casualRun)
}

tasks.register("runReparent") {
    additionalArguments += mapOf("skiko.reparent" to "server")
    dependsOn(casualRun)
}

tasks.withType<Test> {
    systemProperty("skiko.test.screenshots.dir", File(project.projectDir, "src/test/screenshots").absolutePath)

    // Tests should be deterministic, so disable scaling.
    // On MacOs we need the actual scale, otherwise we will have aliased screenshots because of scaling.
    if (System.getProperty("os.name") != "Mac OS X") {
        systemProperty("sun.java2d.dpiaware", "false")
        systemProperty("sun.java2d.uiScale", "1")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
