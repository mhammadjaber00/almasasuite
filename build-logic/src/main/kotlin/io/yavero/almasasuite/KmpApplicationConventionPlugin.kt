package io.yavero.almasasuite

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget


class KmpApplicationConventionPlugin : Plugin<Project> {

    private fun org.gradle.api.artifacts.dsl.DependencyHandler.implementation(dependencyNotation: Any) {
        add("implementation", dependencyNotation)
    }

    override fun apply(target: Project) {
        with(target) {

            val libs = the<VersionCatalogsExtension>().named("libs")


            apply(plugin = "org.jetbrains.kotlin.multiplatform")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")


            extensions.configure<KotlinMultiplatformExtension> {

                when {

                    project.name == "pos-desktop" -> {
                        jvm("desktop")
                    }

                    else -> {
                        jvm()
                    }
                }


                sourceSets.configureEach {
                    val name = this.name

                    if (name == "commonMain") {
                        dependencies {

                            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                            implementation(libs.findLibrary("kotlinx-datetime").get())
                            implementation(libs.findLibrary("kotlinx-serialization-json").get())
                            implementation(project(":shared"))


                            implementation(libs.findLibrary("ktor-client-core").get())
                            implementation(libs.findLibrary("ktor-client-contentNegotiation").get())
                            implementation(libs.findLibrary("ktor-client-auth").get())
                            implementation(libs.findLibrary("ktor-client-logging").get())
                            implementation(libs.findLibrary("ktor-serialization-kotlinx-json").get())
                        }
                    }

                    if (name == "commonTest") {
                        dependencies {

                        implementation(kotlin("test"))
                            implementation(libs.findLibrary("kotest-assertions").get())
                            implementation(libs.findLibrary("kotest-framework-engine").get())
                            implementation(libs.findLibrary("kotest-framework-datatest").get())
                        }
                    }


                    if (name == "jvmMain") {
                        dependencies {
                            implementation(libs.findLibrary("ktor-client-jvm").get())
                        }
                    }

                    if (name == "desktopMain") {
                        dependencies {
                            implementation(libs.findLibrary("ktor-client-jvm").get())
                            implementation(libs.findLibrary("kotlinx-coroutines-swing").get())
                        }
                    }

                }
            }
        }
    }
}