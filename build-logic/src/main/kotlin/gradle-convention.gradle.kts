import com.github.jengelman.gradle.plugins.shadow.ShadowBasePlugin.Companion.shadow
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.gradleup.shadow")
}

group = "com.badminimum.utilities.${project.name}"

repositories {
    mavenCentral()
}

dependencies {
    shadow("org.jspecify:jspecify:1.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.compileOnly.get().extendsFrom(configurations.shadow.get())

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("shadowed")
}

tasks.withType<ShadowJar>().configureEach {
    configurations = listOf(project.configurations.shadow.get())
    archiveClassifier.set("shadowed")
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