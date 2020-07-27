plugins {
    kotlin("multiplatform") version "1.4-M3"
    `cpp-library`
}
group = "org.jetbrains.skiko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                compileOnly("org.projectlombok:lombok:1.18.12")
                compileOnly("org.jetbrains:annotations:19.0.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

// We'd delombok ahead of time now.

/*
dependencies {
    "kapt"("org.projectlombok:lombok:1.18.12")
}
*/

tasks.withType(CppCompile::class.java).configureEach {
    val jdkHome = System.getenv("JAVA_HOME")
    val skiaDir = System.getenv("SKIA_DIR")
    compilerArgs.addAll(listOf(
        "-I$jdkHome/include",
        "-I$jdkHome/include/darwin",
        "-I$skiaDir/include"
        ))
}

extensions.configure<CppLibrary> {
}

library {
    targetMachines.add(machines.macOS.x86_64)
}