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
}
