import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    idea
    `java-library`
    alias(libs.plugins.intellij)
    alias(common.plugins.gradle.dotenv)
    alias(common.plugins.kotlin.jvm)
    alias(common.plugins.kotlin.serialization)
}

group = "dev.kikugie"
version = "0.3.0"

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.commonmark)
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

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(common.misc.commons)
    implementation(common.kotlin.serialization)
    implementation(common.kotlin.serialization.json)

    intellijPlatform {
        instrumentationTools()
        intellijIdeaCommunity(libs.versions.intellij.ce.get())
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.plugins.gradle")
        bundledPlugin("org.jetbrains.kotlin")
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
            freeCompilerArgs.add("-Xjvm-default=all")
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