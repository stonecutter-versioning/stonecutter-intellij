pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.kikugie.dev/releases")
    }

    versionCatalogs {
        register("common") { from("dev.kikugie:stonecutter-versions:1.3.0") }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("2024.3", "2025.3")
    }
}

include("lang")
rootProject.name = "Stonecutter IntelliJ Plugin"
