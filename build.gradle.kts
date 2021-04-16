plugins {
    id("com.gradle.plugin-publish") version "0.14.0"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    `java-library`
    `kotlin-dsl`
    `maven-publish`
}

group = "com.klaxit.hiddensecrets"
version = "0.1.2"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") // Required by detekt
}

dependencies {
    implementation("com.android.tools.build:gradle:4.1.3")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.4.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.3")
    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(15))
    }
}

pluginBundle {
    website = "https://github.com/klaxit/hidden-secrets-gradle-plugin"
    vcsUrl = "https://github.com/klaxit/hidden-secrets-gradle-plugin.git"
    tags = listOf("gradle", "plugin", "android", "hide", "secret", "key", "string", "obfuscate")
}

gradlePlugin {
    plugins {
        create("HiddenSecretsPlugin") {
            id = "com.klaxit.hiddensecrets"
            displayName = "Hidden Secrets Plugin"
            description = "This plugin allows any Android developer to deeply hide secrets in its project to prevent credentials harvesting."
            implementationClass = "com.klaxit.hiddensecrets.HiddenSecretsPlugin"
        }
    }
}

tasks.withType<Copy> {
    //Required by Gradle 7.0
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<Test> {
    useJUnitPlatform()
}