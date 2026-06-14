import gradle.kotlin.dsl.accessors._0cb39c16b209519d61ee18b0fceac003.compileOnly
import gradle.kotlin.dsl.accessors._0cb39c16b209519d61ee18b0fceac003.test
import gradle.kotlin.dsl.accessors._0cb39c16b209519d61ee18b0fceac003.testImplementation
import gradle.kotlin.dsl.accessors._0cb39c16b209519d61ee18b0fceac003.testRuntimeOnly

plugins {
    `java-library`
}

group = "com.badminimum.utilities.${project.name}"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.jetbrains:annotations:26.1.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.apply {
        encoding = "utf-8"
        if (JavaVersion.current().isJava10Compatible) {
            release.set(21)
        }
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}