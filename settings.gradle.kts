pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }

        gradlePluginPortal()
    }

    includeBuild("build-logic")
}

rootProject.name = "Utils"

include("outcome", "datasize")
