plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google {

    mavenContent {
            includeGroupByRegex("androidx.*")
        }
    }
    mavenCentral()
}

dependencies {

implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.dokka.gradlePlugin)
    implementation(libs.sqldelight.gradlePlugin)
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "io.yavero.almasasuite.kmp.library"
            implementationClass = "io.yavero.almasasuite.KmpLibraryConventionPlugin"
        }
        register("kmpApplication") {
            id = "io.yavero.almasasuite.kmp.application"
            implementationClass = "io.yavero.almasasuite.KmpApplicationConventionPlugin"
        }
        register("compose") {
            id = "io.yavero.almasasuite.compose"
            implementationClass = "io.yavero.almasasuite.ComposeConventionPlugin"
        }
        register("detekt") {
            id = "io.yavero.almasasuite.detekt"
            implementationClass = "io.yavero.almasasuite.DetektConventionPlugin"
        }
        register("dokka") {
            id = "io.yavero.almasasuite.dokka"
            implementationClass = "io.yavero.almasasuite.DokkaConventionPlugin"
        }
    }
}