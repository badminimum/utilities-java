pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }

        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"