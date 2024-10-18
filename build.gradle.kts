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
}

dependencies {
    fun plugin(id: String, version: String) = "${id}:${id}.gradle.plugin:${version}"

    runtimeOnly("org.slf4j:slf4j-simple:1.7.10")
    implementation(plugin("dev.kikugie.stonecutter", "0.5-beta.2"))
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

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = "stonecutter-dev"
    version = "2024.1.4"
    type = "IC" // Target IDE Platform
    plugins = listOf("com.intellij.java", "org.jetbrains.plugins.gradle")
}

tasks.patchPluginXml {
    sinceBuild = "223"
    untilBuild = "242.*"
    version = project.version.toString()
}