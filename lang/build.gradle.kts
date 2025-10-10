plugins {
    antlr
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    antlr(libs.antlr)
}

tasks {
    generateGrammarSource {
        arguments = arguments + listOf("-long-messages", "-lib", "$projectDir/src/main/antlr/dev/kikugie/stonecutter/intellij/lang/impl")
    }
}