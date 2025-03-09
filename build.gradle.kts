import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    idea
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.intellij)
    alias(libs.plugins.dotenv)
}

group = "dev.kikugie"
version = "0.1.2"

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.commonmark:commonmark:0.24.0")
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.gradle.org/gradle/libs-releases")
    maven("https://maven.kikugie.dev/snapshots")

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    fun gradle(id: String, version: String) = "${id}:${id}.gradle.plugin:${version}"

    implementation(gradle("dev.kikugie.stonecutter", "0.6-alpha.8"))
    implementation("org.apache.commons:commons-text:1.13.0")
    intellijPlatform {
        instrumentationTools()
        intellijIdeaCommunity("2024.2.3")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.plugins.gradle")
        plugin("PsiViewer:243.7768")
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
    fun fileProperty(path: String) = provider { file(path) }

    fun File.mdtoHtml(): String {
        val parser = Parser.builder().build()
        val document = reader().use { parser.parseReader(it) }
        val renderer = HtmlRenderer.builder().build()
        return renderer.render(document)
    }

    buildSearchableOptions = false
    pluginConfiguration {
        id = "dev.kikugie.stonecutter"
        name = "Stonecutter Dev"
        version = project.version.toString()
        description = fileProperty("README.md").map { it.mdtoHtml() }
        changeNotes = fileProperty("CHANGELOG.md").map { it.mdtoHtml() }

        ideaVersion {
            sinceBuild = "241"
            untilBuild = "251.*"
        }

        vendor {
            name = "KikuGie"
            email = "git.kikugie@protonmail.com"
            url = "https://stonecutter.kikugie.dev/"
        }
    }

    publishing {
        token = env.fetch("PUBLISH", "")
    }
}