package io.yavero.almasasuite

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType


class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            apply(plugin = "io.gitlab.arturbosch.detekt")


            extensions.configure<DetektExtension> {

                val baselineFile = rootProject.file("config/detekt/baseline.xml")


                if (!baselineFile.exists()) {
                    baselineFile.parentFile.mkdirs()
                    baselineFile.createNewFile()
                }

                baseline = baselineFile


                config = files("${rootProject.projectDir}/config/detekt/detekt.yml")
                buildUponDefaultConfig = true
                allRules = false


                parallel = true


                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    txt.required.set(false)
                    sarif.required.set(true)
                    md.required.set(false)
                }
            }


            tasks.withType<Detekt>().configureEach {

                setSource(files("src"))


                exclude("**/build/**", "**/resources/**", "**/generated/**")


                parallel = true


                include("**/*.kt", "**/*.kts")
                exclude("**/build/**", "**/test/**")


                reports {
                    xml.required.set(true)
                    xml.outputLocation.set(file("build/reports/detekt/detekt.xml"))
                    html.required.set(true)
                    html.outputLocation.set(file("build/reports/detekt/detekt.html"))
                    sarif.required.set(true)
                    sarif.outputLocation.set(file("build/reports/detekt/detekt.sarif"))
                }
            }


            if (!tasks.names.contains("detektBaseline")) {
                tasks.register<DetektCreateBaselineTask>("detektBaseline") {
                    description = "Creates a detekt baseline for this project"
                    buildUponDefaultConfig.set(true)
                    ignoreFailures.set(true)
                    parallel.set(true)
                    setSource(files("src"))
                    config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
                    baseline.set(file("${rootProject.projectDir}/config/detekt/baseline.xml"))
                    include("**/*.kt", "**/*.kts")
                    exclude("**/build/**", "**/test/**", "**/resources/**", "**/generated/**")
                }
            }
        }
    }
}