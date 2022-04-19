plugins {
    id("org.jetbrains.gradle.apple.applePlugin") version "222.849-0.15.1"
}

apple {
    iosApp {
        productName = "kmmappcode"

        sceneDelegateClass = "SceneDelegate"
        launchStoryboard = "LaunchScreen"

        dependencies {
            implementation(project(":shared"))
        }
    }
}
