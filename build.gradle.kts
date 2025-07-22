import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    idea
    `java-library`
    alias(libs.plugins.intellij)
    alias(libs.plugins.grammarkit)
    alias(common.plugins.gradle.dotenv)
    alias(common.plugins.kotlin.jvm)
    alias(common.plugins.kotlin.serialization)
}

group = "dev.kikugie"
version = "0.5.3"

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
        java.srcDirs(layout.buildDirectory.files("generated/lexer", "generated/parser"))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.kikugie.dev/releases")
    maven("https://repo.gradle.org/gradle/libs-releases")
    maven("https://central.sonatype.com/repository/maven-snapshots/")

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(libs.semver)
    implementation(common.misc.commons)
    implementation(common.kotlin.serialization)
    implementation(common.kotlin.serialization.json)

    intellijPlatform {
        intellijIdeaCommunity(libs.versions.intellij.ce.get())
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.jetbrains.plugins.gradle")
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

        dependsOn(generateLexer, generateParser)
    }

    generateLexer {
        sourceFile = file("src/main/kotlin/dev/kikugie/stonecutter/intellij/lang/impl/StitcherImplLexer.flex")
        targetOutputDir = layout.buildDirectory.dir("generated/lexer/dev/kikugie/stonecutter/intellij/lang/impl")
        purgeOldFiles = true
    }

    generateParser {
        sourceFile = file("src/main/kotlin/dev/kikugie/stonecutter/intellij/lang/impl/StitcherImplParser.bnf")
        targetRootOutputDir = layout.buildDirectory.dir("generated/parser")
        pathToParser = "" // Defined in the .bnf
        pathToPsiRoot = "" // Defined in the .bnf
        purgeOldFiles = true
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
