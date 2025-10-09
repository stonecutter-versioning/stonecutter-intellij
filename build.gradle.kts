import org.apache.tools.ant.filters.ReplaceTokens
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.intellij)
    alias(common.plugins.kotlin.jvm)
    alias(common.plugins.kotlin.serialization)
}

version = "0.8+${property("intellij.suffix")}"

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.commonmark)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.kikugie.dev/releases/")
    maven("https://maven.kikugie.dev/third-party/")
    maven("https://repo.gradle.org/gradle/libs-releases/")
    maven("https://central.sonatype.com/repository/maven-snapshots/")

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":lang"))
    implementation(libs.antlr.adapter)
    implementation(common.misc.semver)
    implementation(common.misc.commons)
    implementation(common.kotlin.serialization)
    implementation(common.kotlin.serialization.json)

    intellijPlatform {
        intellijIdeaUltimate(property("intellij.version") as String)
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.jetbrains.plugins.gradle")
//        plugin("com.demonwav.minecraft-dev:2024.3-1.8.5-576@nightly")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    @Suppress("UnstableApiUsage")
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

tasks {
    runIde {
        jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:ReservedCodeCacheSize=1G")
    }

    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjvm-default=all")
            freeCompilerArgs.add("-Xnested-type-aliases")
        }
    }

    processResources {
        val sync = if (stonecutter.eval(stonecutter.current.version, ">=2025")) "no-op"
        else "--><syncContributor implementation=\"dev.kikugie.stonecutter.intellij.service.gradle.GradleReloadListener\"/><!--"

        filesMatching("**/plugin.xml") {
            filter<ReplaceTokens>("tokens" to mapOf("sync-contributor" to sync))
        }
    }

    patchPluginXml {
        dependsOn("stonecutterGenerate")
    }
}

intellijPlatform {
    val parser = Parser.builder().build()
    val renderer = HtmlRenderer.builder().build()

    fun env(str: String): Map<String, String> = buildMap {
        for (line in str.lineSequence().filter(String::isNotBlank)) {
            val (key, value) = line.split('=', limit = 2)
            this[key] = value.trim(' ', '"')
        }
    }

    fun env(contents: File): Map<String, String> =
        contents.readText().let(::env)

    fun mdToHtml(contents: File): String =
        contents.readText().let { parser.parse(it).let(renderer::render) }

    buildSearchableOptions = false
    pluginConfiguration {
        id = "dev.kikugie.stonecutter"
        name = "Stonecutter Dev"
        version = project.version.toString()
        description = rootProject.file("README.md").let(::mdToHtml)
        changeNotes = rootProject.file("CHANGELOG.md").let(::mdToHtml)

        ideaVersion {
            sinceBuild = property("intellij.min") as String
            untilBuild = property("intellij.max") as String
        }

        vendor {
            name = "KikuGie"
            email = "git.kikugie@protonmail.com"
            url = "https://stonecutter.kikugie.dev/"
        }
    }

    publishing {
        token = rootProject.file(".env").let(::env)["PUBLISH"]
    }
}
