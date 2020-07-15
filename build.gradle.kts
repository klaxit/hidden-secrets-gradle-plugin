plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "com.klaxit.hiddensecrets"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codehaus.groovy:groovy-all:2.5.8")
    //testImplementation("junit", "junit", "4.12")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.10")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("HiddenSecretsPlugin") {
            id = "com.klaxit.hiddensecrets"
            implementationClass = "com.klaxit.hiddensecrets.HiddenSecretsPlugin"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}