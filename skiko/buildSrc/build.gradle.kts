plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(gradleApi())
    implementation(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    implementation(libs.android.gradlePlugin)
    implementation(libs.dokka.gradlePlugin)
    implementation(libs.buildHelpers.publishing.gradlePlugin)

    implementation(libs.gradleDownloadTask.gradlePlugin)
    implementation(libs.githubApi)
    implementation(libs.crypto.checksum.gradlePlugin)
    implementation(libs.kotlinx.benchmark.gradlePlugin)
}
