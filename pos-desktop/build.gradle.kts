plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}



kotlin {

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {

                implementation(projects.shared)


                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)


                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.serialization.kotlinx.json)


                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.compose.material3)
            }
        }

        val desktopMain by getting {
            dependencies {

                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.jvm)
                implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.jvmDriver)


                implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            }
        }

        val commonTest by getting {
            dependencies {

            implementation(kotlin("test"))
                implementation(libs.kotest.assertions)
                implementation(libs.kotest.framework.engine)
            }
        }
    }
}


compose.desktop {
    application {
        mainClass = "io.yavero.almasasuite.pos.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                          org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "io.yavero.almasasuite.pos"
            packageVersion = "1.0.0"


            windows {
                menuGroup = "ALMASA Suite"
            }

            macOS {
                bundleID = "io.yavero.almasasuite.pos"
            }

            linux {
                menuGroup = "ALMASA Suite"
            }
        }


    }
}


detekt {
    baseline = rootProject.file("config/detekt/baseline.xml")
    config = files("${rootProject.projectDir}/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}


tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(file("${rootProject.buildDir}/dokka"))
}