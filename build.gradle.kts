import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.intellij)
}

group = "dev.kikugie"
version = "0.1.0-beta.3"

repositories {
    mavenCentral()
    maven("https://repo.gradle.org/gradle/libs-releases")
    maven("https://maven.kikugie.dev/snapshots")

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    fun plugin(id: String, version: String) = "${id}:${id}.gradle.plugin:${version}"

    runtimeOnly("org.slf4j:slf4j-simple:1.7.10")
    implementation(plugin("dev.kikugie.stonecutter", "0.5-beta.3"))
    intellijPlatform {
        instrumentationTools()
        intellijIdeaCommunity("2024.2.3")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.plugins.gradle")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_16)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "stonecutter-dev"
        name = "Stonecutter Dev"
        version = project.version.toString()
        description = """
            Multi-version management Gradle plugin.
            
            Stonecutter is a Gradle plugin that allows working on a project that targets multiple compatible release versions of a dependency.
            It's mostly meant for Minecraft mods, but on itself the plugin is platform-independent.
            This project is inspired by <a href="https://github.com/ReplayMod/preprocessor">Preprocessor</a>, <a href="https://github.com/raydac/java-comment-preprocessor">JCP</a> and <a href="https://github.com/SHsuperCM/Stonecutter">The original Stonecutter</a>,
            expanding on their features and providing new ones.
            
            The intellij plugin provides addition functionality for the Gradle plugin.
            For more information visit the <a href="https://stonecutter.kikugie.dev/">Stonecutter website</a>.
        """.trimIndent()
        changeNotes = file("CHANGELOG.md").readText()

        ideaVersion {
            sinceBuild = "223"
            untilBuild = "242.*"
        }

        vendor {
            name = "KikuGie"
            email = "git.kikugie@protonmail.com"
            url = "https://stonecutter.kikugie.dev/"
        }
    }
}