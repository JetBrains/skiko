plugins {
    org.jetbrains.dokka
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
}

dokka {
    dokkaPublications.html {
        moduleName.set("skiko")
    }
}

dependencies {
    dokka(project(":"))
    dokka(project(":skiko-skottie"))
}