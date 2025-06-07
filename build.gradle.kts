import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    idea
//    antlr
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.intellij)
    alias(libs.plugins.dotenv)
}

group = "dev.kikugie"
version = "0.2"

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

sourceSets {
    main {
        java.srcDir("src/main/gen")
    }
}

repositories {
    mavenCentral()
    maven("https://repo.gradle.org/gradle/libs-releases")
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    maven("https://maven.kikugie.dev/snapshots")

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(libs.commons)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.serialization.json)

    intellijPlatform {
        instrumentationTools()
        intellijIdeaCommunity("2024.3.6")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.plugins.gradle")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
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
            sinceBuild = "243"
            untilBuild = "252.*"
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