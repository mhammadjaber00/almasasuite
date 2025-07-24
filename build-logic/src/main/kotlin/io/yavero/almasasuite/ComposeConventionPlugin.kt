package io.yavero.almasasuite

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl


class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            val libs = the<VersionCatalogsExtension>().named("libs")


            apply(plugin = "org.jetbrains.compose")










            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.configureEach {
                    val name = this.name

                    if (name == "commonMain") {
                        dependencies {

                            implementation("org.jetbrains.compose.runtime:runtime:${libs.findVersion("composeMultiplatform").get()}")
                            implementation("org.jetbrains.compose.foundation:foundation:${libs.findVersion("composeMultiplatform").get()}")
                            implementation("org.jetbrains.compose.material3:material3:${libs.findVersion("composeMultiplatform").get()}")
                            implementation("org.jetbrains.compose.ui:ui:${libs.findVersion("composeMultiplatform").get()}")
                            implementation("org.jetbrains.compose.components:components-resources:${libs.findVersion("composeMultiplatform").get()}")


                            implementation(libs.findLibrary("compose-material3").get())
                        }
                    }


                    if (name == "desktopMain") {
                        dependencies {
                            implementation("org.jetbrains.compose.desktop:desktop:${libs.findVersion("composeMultiplatform").get()}")
                            implementation("org.jetbrains.compose.ui:ui-tooling-preview:${libs.findVersion("composeMultiplatform").get()}")
                        }
                    }

                    if (name == "jvmMain") {
                        dependencies {
                            implementation("org.jetbrains.compose.ui:ui-tooling-preview:${libs.findVersion("composeMultiplatform").get()}")
                        }
                    }
                }
            }



            if (project.name == "pos-desktop") {
                logger.lifecycle("Note: For the pos-desktop module, the compose.desktop configuration is already handled in the build.gradle.kts file.")
                logger.lifecycle("Make sure the window size is set to 1280x800 as per requirements.")
            }
        }
    }


    private fun org.gradle.api.artifacts.dsl.DependencyHandler.implementation(dependencyNotation: Any) {
        add("implementation", dependencyNotation)
    }
}