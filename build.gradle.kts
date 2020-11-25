plugins {
    id("com.gradle.plugin-publish") version "0.12.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "com.klaxit.hiddensecrets"
version = "0.1.0"

repositories {
    mavenCentral()
    google()
    jcenter()
}

dependencies {
    implementation("com.android.tools.build:gradle:4.0.0")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.10")
    testImplementation("junit:junit:4.13")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
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