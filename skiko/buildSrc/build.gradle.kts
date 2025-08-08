plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(gradleApi())
    implementation(kotlin("gradle-plugin", "1.9.21"))
    implementation("de.undercouch:gradle-download-task:5.5.0")
    implementation("org.gradle.crypto.checksum:org.gradle.crypto.checksum.gradle.plugin:1.4.0")
}
