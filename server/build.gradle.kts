plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    application
}

group = "io.yavero.almasasuite"
version = "1.0.0"
application {
    mainClass.set("io.yavero.almasasuite.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)


    implementation(libs.logback)


    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.statusPages)


    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgres.jdbc)
    implementation(libs.flyway.core)
    implementation("com.h2database:h2:2.2.224")


    implementation(libs.bcrypt)


    implementation(libs.stripe.java)


    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgres)
}