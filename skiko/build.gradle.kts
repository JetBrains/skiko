import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import tasks.configuration.*
import kotlin.collections.HashMap
import declareSkiaTasks

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka") version "1.9.10"
    `maven-publish`
    signing
    id("org.gradle.crypto.checksum") version "1.4.0"
}

apply<WasmImportsGeneratorCompilerPluginSupportPlugin>()
apply<WasmImportsGeneratorForTestCompilerPluginSupportPlugin>()

val coroutinesVersion = "1.8.0"
val atomicfuVersion = "0.23.2"

val skiko = SkikoProperties(rootProject)
val buildType = skiko.buildType
val targetOs = hostOs
val targetArch = skiko.targetArch


val skikoProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, targetArch)
    },
    createChecksumsTask = { targetOs: OS, targetArch: Arch, fileToChecksum: Provider<File> ->
        createChecksumsTask(targetOs, targetArch, fileToChecksum)
    }
)

allprojects {
    group = SkikoArtifacts.groupId
    version = skiko.deployVersion
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

kotlin {
    skikoProjectContext.declareSkiaTasks()

    if (supportAwt) {
        jvm("awt") {
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
            generateVersion(targetOs, targetArch, skiko)
        }
    }

    if (supportAndroid) {
        jvm("android") {
            withJava() // This line needs to add Java sources in src/androidMain/java
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
            // We need an additional attribute to distinguish between JVM variants.
            attributes {
                attributes.attribute(Attribute.of("ui", String::class.java), "android")
            }
            // TODO: seems incorrect.
            generateVersion( OS.Android, Arch.Arm64, skiko)
        }
    }

    if (supportJs) {
        js(IR) {
            moduleName = "skiko-kjs" // override the name to avoid name collision with a different skiko.js file
            browser {
                testTask {
                    dependsOn("linkWasm")
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(project.projectDir.resolve("karma.config.d").resolve("js"))
                    }
                }
            }
            binaries.executable()
            generateVersion(OS.Wasm, Arch.Wasm, skiko)
        }
    }

    if (supportWasm) {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            moduleName = "skiko-kjs-wasm" // override the name to avoid name collision with a different skiko.js file
            browser {
                testTask {
                    dependsOn("linkWasm")
                    useKarma {
                        this.webpackConfig.experiments.add("topLevelAwait")
                        useChromeHeadless()
                        useConfigDirectory(project.projectDir.resolve("karma.config.d").resolve("wasm"))
                    }
                }
            }
            generateVersion(OS.Wasm, Arch.Wasm, skiko)

            val main by compilations.getting
            val test by compilations.getting

            val linkWasmTasks = skikoProjectContext.createWasmLinkTasks()
            project.tasks.named<Copy>(test.processResourcesTaskName) {
                from(linkWasmTasks.linkWasm!!) {
                    include("*.wasm")
                }

                from(linkWasmTasks.linkWasmWithES6!!) {
                    include("*.mjs")
                }

                from(skikoTestMjs)
                dependsOn(test.compileTaskProvider)
            }

            setupImportsGeneratorPlugin()
        }
    }

    if (supportNativeMac) {
        skikoProjectContext.configureNativeTarget(OS.MacOS, Arch.X64, macosX64())
        skikoProjectContext.configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64())
    }
    if (supportNativeLinux) {
        skikoProjectContext.configureNativeTarget(OS.Linux, Arch.X64, linuxX64())
    }
    if (supportNativeIosArm64) {
        skikoProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64())
    }
    if (supportNativeIosSimulatorArm64) {
        skikoProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosSimulatorArm64())
    }
    if (supportNativeIosX64) {
        skikoProjectContext.configureNativeTarget(OS.IOS, Arch.X64, iosX64())
    }
    if (supportNativeTvosArm64) {
        skikoProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosArm64())
    }
    if (supportNativeTvosSimulatorArm64) {
        skikoProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosSimulatorArm64())
    }
    if (supportNativeTvosX64) {
        skikoProjectContext.configureNativeTarget(OS.TVOS, Arch.X64, tvosX64())
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("com.jetbrains:jbr-api:1.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
            }
        }
        if (supportAwt) {
            val awtMain by getting {
                dependsOn(jvmMain)
            }
        }

        if (supportAndroid) {
            val androidMain by getting {
                dependsOn(jvmMain)
                dependencies {
                    compileOnly(files(androidJar()))
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
                }
            }
        }

        val jvmTest by creating {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                implementation(kotlin("test-junit"))
                implementation(kotlin("test"))
            }
        }

        if (supportAwt) {
            val awtTest by getting {
                dependsOn(jvmTest)
            }
        }

        if (supportAndroid) {
            val androidTest by getting {
                dependsOn(jvmTest)
            }
        }

        if (supportJs || supportWasm || supportAnyNative) {
            val nativeJsMain by creating {
                dependsOn(commonMain)
            }

            val nativeJsTest by creating {
                dependsOn(commonTest)
            }

            if (supportJs || supportWasm) {
                val jsWasmMain by creating {
                    dependsOn(nativeJsMain)
                }

                val jsWasmTest by creating {
                    dependsOn(nativeJsTest)
                }

                if (supportJs) {
                    val jsMain by getting {
                        dependsOn(jsWasmMain)
                    }

                    val jsTest by getting {
                        dependsOn(jsWasmTest)
                    }
                }

                if (supportWasm) {
                    val wasmJsMain by getting {
                        dependsOn(jsWasmMain)
                    }
                    val wasmJsTest by getting {
                        dependsOn(jsWasmTest)

                        dependencies {
                            implementation(kotlin("test-wasm-js"))
                        }
                    }
                }
            }

            if (supportAnyNative) {
                all {
                    // Really ugly, see https://youtrack.jetbrains.com/issue/KT-46649 why it is required,
                    // note that setting it per source set still keeps it unset in commonized source sets.
                    languageSettings.optIn("kotlin.native.SymbolNameIsInternal")
                }
                // See https://kotlinlang.org/docs/mpp-share-on-platforms.html#configure-the-hierarchical-structure-manually
                val nativeMain by creating {
                    dependsOn(nativeJsMain)
                    dependencies {
                        // TODO: remove this explicit dependency on atomicfu
                        // after this is fixed https://jetbrains.slack.com/archives/C3TNY2MM5/p1701462109621819
                        implementation("org.jetbrains.kotlinx:atomicfu:$atomicfuVersion")
                    }
                }
                val nativeTest by creating {
                    dependsOn(nativeJsTest)
                }
                if (supportNativeLinux) {
                    val linuxMain by creating {
                        dependsOn(nativeMain)
                    }
                    val linuxTest by creating {
                        dependsOn(nativeTest)
                    }
                    val linuxX64Main by getting {
                        dependsOn(linuxMain)
                    }
                    val linuxX64Test by getting {
                        dependsOn(linuxTest)
                    }
                }
                if (supportAnyNativeIos || supportNativeMac) {
                    val darwinMain by creating {
                        dependsOn(nativeMain)
                    }
                    val darwinTest by creating {
                        dependsOn(nativeTest)
                    }
                    if (supportNativeMac) {
                        val macosMain by creating {
                            dependsOn(darwinMain)
                        }
                        val macosTest by creating {
                            dependsOn(darwinTest)
                        }
                        val macosX64Main by getting {
                            dependsOn(macosMain)
                        }
                        val macosX64Test by getting {
                            dependsOn(macosTest)
                        }
                        val macosArm64Main by getting {
                            dependsOn(macosMain)
                        }
                        val macosArm64Test by getting {
                            dependsOn(macosTest)
                        }
                    }
                    if (supportAnyNativeIos || supportAllNativeTvos) {
                        val uikitMain by creating {
                            dependsOn(darwinMain)
                        }
                        val uikitTest by creating {
                            dependsOn(darwinTest)
                        }

                        if (supportAnyNativeIos) {
                            val iosMain by creating {
                                dependsOn(uikitMain)
                            }
                            val iosTest by creating {
                                dependsOn(uikitTest)
                            }
                            if (supportNativeIosArm64) {
                                val iosArm64Main by getting {
                                    dependsOn(iosMain)
                                }
                                val iosArm64Test by getting {
                                    dependsOn(iosTest)
                                }
                            }
                            if (supportNativeIosSimulatorArm64) {
                                val iosSimulatorArm64Main by getting {
                                    dependsOn(iosMain)
                                }
                                val iosSimulatorArm64Test by getting {
                                    dependsOn(iosTest)
                                }
                            }
                            if (supportNativeIosX64) {
                                val iosX64Main by getting {
                                    dependsOn(iosMain)
                                }
                                val iosX64Test by getting {
                                    dependsOn(iosTest)
                                }
                            }
                        }
                        if (supportAnyNativeTvos) {
                            val tvosMain by creating {
                                dependsOn(uikitMain)
                            }
                            val tvosTest by creating {
                                dependsOn(uikitTest)
                            }
                            if (supportNativeTvosArm64) {
                                val tvosArm64Main by getting {
                                    dependsOn(tvosMain)
                                }
                                val tvosArm64Test by getting {
                                    dependsOn(tvosTest)
                                }
                            }
                            if (supportNativeTvosSimulatorArm64) {
                                val tvosSimulatorArm64Main by getting {
                                    dependsOn(tvosMain)
                                }
                                val tvosSimulatorArm64Test by getting {
                                    dependsOn(tvosTest)
                                }
                            }
                            if (supportNativeTvosX64) {
                                val tvosX64Main by getting {
                                    dependsOn(tvosMain)
                                }
                                val tvosX64Test by getting {
                                    dependsOn(tvosTest)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    configureIOSTestsWithMetal(project)
}

if (supportAndroid) {
    val os = OS.Android
    val skikoAndroidJar by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-android")
        from(kotlin.jvm("android").compilations["main"].output.allOutputs)
    }
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        skikoProjectContext.createSkikoJvmJarTask(os, arch, skikoAndroidJar)
    }
    tasks.getByName("publishAndroidPublicationToMavenLocal") {
        // It needs to be compatible with Gradle 8.1
        dependsOn(skikoAndroidJar)
    }
    tasks.getByName("generateMetadataFileForAndroidPublication") {
        // It needs to be compatible with Gradle 8.1
        dependsOn(skikoAndroidJar)
    }
}

// Can't be moved to buildSrc because of Checksum dependency
fun createChecksumsTask(
    targetOs: OS,
    targetArch: Arch,
    fileToChecksum: Provider<File>
) = project.registerSkikoTask<Checksum>("createChecksums", targetOs, targetArch) {

    files = project.files(fileToChecksum)
    algorithm = Checksum.Algorithm.SHA256
    outputDir = file("$buildDir/checksums-${targetId(targetOs, targetArch)}")
}


if (supportAwt) {
    val skikoAwtJarForTests by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-awt-test")
        from(kotlin.jvm("awt").compilations["main"].output.allOutputs)
    }
    skikoProjectContext.setupJvmTestTask(skikoAwtJarForTests, targetOs, targetArch)
}

afterEvaluate {
    tasks.configureEach {
        if (group == "publishing") {
            // There are many intermediate tasks in 'publishing' group.
            // There are a lot of them and they have verbose names.
            // To decrease noise in './gradlew tasks' output and Intellij Gradle tool window,
            // group verbose tasks in a separate group 'other publishing'.
            val allRepositories = publishing.repositories.map { it.name } + "MavenLocal"
            val publishToTasks = allRepositories.map { "publishTo$it" }
            if (name != "publish" && name !in publishToTasks) {
                group = "other publishing"
            }
        }
    }

    tasks.named("clean").configure {
        doLast {
            delete(skiko.dependenciesDir)
            delete(project.file("src/jvmMain/java"))
        }
    }
}

val emptySourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        configureEach {
            val repoName = name
            tasks.register("publishTo${repoName}") {
                group = "publishing"
                dependsOn(tasks.named("publishAllPublicationsTo${repoName}Repository"))
            }
        }
        maven {
            name = "BuildRepo"
            url = uri("${rootProject.buildDir}/repo")
        }
        maven {
            name = "ComposeRepo"
            url = uri(skiko.composeRepoUrl)
            credentials {
                username = skiko.composeRepoUserName
                password = skiko.composeRepoKey
            }
        }
    }
    publications {
        val pomNameForPublication = HashMap<String, String>()
        pomNameForPublication["kotlinMultiplatform"] = "Skiko MPP"
        kotlin.targets.forEach {
            pomNameForPublication[it.name] = "Skiko ${toTitleCase(it.name)}"
        }
        configureEach {
            this as MavenPublication
            groupId = SkikoArtifacts.groupId

            // Necessary for publishing to Maven Central
            artifact(emptyJavadocJar)

            pom {
                description.set("Kotlin Skia bindings")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                val repoUrl = "https://www.github.com/JetBrains/skiko"
                url.set(repoUrl)
                scm {
                    url.set(repoUrl)
                    val repoConnection = "scm:git:$repoUrl.git"
                    connection.set(repoConnection)
                    developerConnection.set(repoConnection)
                }
                developers {
                    developer {
                        name.set("Compose Multiplatform Team")
                        organization.set("JetBrains")
                        organizationUrl.set("https://www.jetbrains.com")
                    }
                }
            }
        }

        skikoProjectContext.allJvmRuntimeJars.forEach { entry ->
            val os = entry.key.first
            val arch = entry.key.second
            create<MavenPublication>("skikoJvmRuntime${toTitleCase(os.id)}${toTitleCase(arch.id)}") {
                pomNameForPublication[name] = "Skiko JVM Runtime for ${os.name} ${arch.name}"
                artifactId = SkikoArtifacts.jvmRuntimeArtifactIdFor(os, arch)
                afterEvaluate {
                    artifact(entry.value.map { it.archiveFile.get() })
                    artifact(emptySourcesJar)
                }
                pom.withXml {
                    asNode().appendNode("dependencies")
                        .appendNode("dependency").apply {
                            appendNode("groupId", SkikoArtifacts.groupId)
                            appendNode("artifactId", SkikoArtifacts.jvmArtifactId)
                            appendNode("version", skiko.deployVersion)
                            appendNode("scope", "compile")
                        }
                }
            }
        }

        if (supportJs || supportWasm) {
            create<MavenPublication>("skikoWasmRuntime") {
                pomNameForPublication[name] = "Skiko WASM Runtime"
                artifactId = SkikoArtifacts.jsWasmArtifactId
                artifact(tasks.named("skikoWasmJar").get())
                artifact(emptySourcesJar)
            }
        }

        val publicationsWithoutPomNames = publications.filter { it.name !in pomNameForPublication }
        if (publicationsWithoutPomNames.isNotEmpty()) {
            error("Publications with unknown POM names: ${publicationsWithoutPomNames.joinToString { "'$it'" }}")
        }
        configureEach {
            this as MavenPublication
            pom.name.set(pomNameForPublication[name]!!)
        }
    }
}

val mavenCentral = MavenCentralProperties(project)
if (skiko.isTeamcityCIBuild || mavenCentral.signArtifacts) {
    signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(mavenCentral.signArtifactsKey.get(), mavenCentral.signArtifactsPassword.get())
    }
    configureSignAndPublishDependencies()
}

tasks.withType<AbstractTestTask> {
    testLogging {
        events("FAILED", "SKIPPED")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
    }
}

tasks.withType<JavaCompile> {
    // Workaround to configure Java sources on Android (src/androidMain/java)
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}

project.tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xwasm-enable-array-range-checks", "-Xir-dce=true", "-Xskip-prerelease-check",
    )
}

tasks.findByName("publishSkikoWasmRuntimePublicationToComposeRepoRepository")
    ?.dependsOn("publishWasmJsPublicationToComposeRepoRepository")
tasks.findByName("publishSkikoWasmRuntimePublicationToMavenLocal")
    ?.dependsOn("publishWasmJsPublicationToMavenLocal")


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
    // https://youtrack.jetbrains.com/issue/KT-56583
    compilerOptions.freeCompilerArgs.add("-XXLanguage:+ImplicitSignedToUnsignedIntegerConversion")
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.cinterop.ExperimentalForeignApi"
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
}
