plugins {
    `kotlin-dsl`
    `version-catalog`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.bundles.gradle.plugins)
}