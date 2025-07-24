package io.yavero.almasasuite

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget


class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            val libs = the<VersionCatalogsExtension>().named("libs")


            apply(plugin = "org.jetbrains.kotlin.multiplatform")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")





            extensions.configure<KotlinMultiplatformExtension> {

                jvm()


                sourceSets.configureEach {
                    val name = this.name

                    if (name == "commonMain") {
                        dependencies {

                        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                            implementation(libs.findLibrary("kotlinx-datetime").get())
                            implementation(libs.findLibrary("kotlinx-serialization-json").get())
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

                            implementation(libs.findLibrary("sqldelight-jvmDriver").get())


                            if (project.name == "server") {
                                implementation(libs.findLibrary("sqldelight-postgresDriver").get())
                            }
                        }
                    }

                    if (name == "jvmTest") {
                        dependencies {
                            implementation(libs.findLibrary("kotest-runner-junit5").get())
                        }
                    }

                }
            }


        }
    }


    private fun org.gradle.api.artifacts.dsl.DependencyHandler.implementation(dependencyNotation: Any) {
        add("implementation", dependencyNotation)
    }
}