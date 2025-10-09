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
        arguments = arguments + listOf("-visitor", "-long-messages")
    }
}