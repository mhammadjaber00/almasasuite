package io.yavero.almasasuite

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.io.File


class DokkaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            apply(plugin = "org.jetbrains.dokka")


            tasks.withType<DokkaTask>().configureEach {

                outputDirectory.set(file("${rootProject.buildDir}/dokka"))


                dokkaSourceSets.configureEach {

                    sourceRoot(file("src"))


                    if (project.name != "build-logic") {

                        externalDocumentationLink {
                            url.set(uri("https://kotlinlang.org/api/latest/jvm/stdlib/").toURL())
                        }


                        externalDocumentationLink {
                            url.set(uri("https://api.ktor.io/").toURL())
                        }


                        externalDocumentationLink {
                            url.set(uri("https://www.jetbrains.com/help/compose-multiplatform/").toURL())
                        }
                    }


                    includes.from(
                        listOfNotNull(
                            "README.md",
                            "MODULE.md"
                        ).map { fileName ->
                            File(project.projectDir, fileName)
                                .takeIf { it.exists() }
                                ?.absolutePath
                        }
                    )
                }
            }


            tasks.withType<DokkaTaskPartial>().configureEach {

                outputDirectory.set(file("${project.buildDir}/dokka-partial"))


                dokkaSourceSets.configureEach {

                    sourceRoot(file("src"))


                    if (project.name != "build-logic") {

                        externalDocumentationLink {
                            url.set(uri("https://kotlinlang.org/api/latest/jvm/stdlib/").toURL())
                        }


                        externalDocumentationLink {
                            url.set(uri("https://api.ktor.io/").toURL())
                        }


                        externalDocumentationLink {
                            url.set(uri("https://www.jetbrains.com/help/compose-multiplatform/").toURL())
                        }
                    }


                    includes.from(
                        listOfNotNull(
                            "README.md",
                            "MODULE.md"
                        ).map { fileName ->
                            File(project.projectDir, fileName)
                                .takeIf { it.exists() }
                                ?.absolutePath
                        }
                    )
                }
            }


            if (project == rootProject) {
                tasks.register("dokkaHtmlMultiModule", org.jetbrains.dokka.gradle.DokkaMultiModuleTask::class.java) {
                    outputDirectory.set(file("${buildDir}/dokka"))


                }
            }
        }
    }
}