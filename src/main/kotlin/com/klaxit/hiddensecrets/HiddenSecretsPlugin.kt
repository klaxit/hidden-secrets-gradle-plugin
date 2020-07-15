package com.klaxit.hiddensecrets

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import java.io.File
import java.nio.charset.Charset

open class HiddenSecretsPluginExtension {
    var packageName: String = ""
}

/**
 * Available gradle tasks from HiddenSecretsPlugin
 */
open class HiddenSecretsPlugin : Plugin<Project> {
    companion object {
        private const val APP_MAIN_FOLDER = "src/main/"
        //Tasks
        private const val TASK_UNZIP_HIDDEN_SECRETS = "unzipHiddenSecrets"
        private const val TASK_COPY_CPP = "copyCpp"
        private const val TASK_COPY_KOTLIN = "copyKotlin"
        private const val TASK_OBFUSCATE_KEY = "obfuscateKey"
        private const val TASK_SETUP_HIDDEN_SECRETS = "setupHiddenSecrets"
        private const val TASK_HIDE_SECRET_KEY = "hideSecretKey"
    }

    override fun apply(project: Project) {
        val tmpFolder = java.lang.String.format("%s/hidden-secrets-tmp", project.buildDir)

        val hiddenExtension = project.extensions.create<HiddenSecretsPluginExtension>(
                "hidden", HiddenSecretsPluginExtension::class.java
        )

        /**
         * Get key param from command line
         */
        @Input
        fun getKeyParam(): String {
            var key = ""
            if (project.hasProperty("key")) {
                //From command line
                key = project.property("key") as String
            }
            return key
        }

        /**
         * Get package name param from command line
         */
        @Input
        fun getPackageNameParam(): String {
            var packageName = hiddenExtension.packageName
            if (project.hasProperty("package")) {
                //From command line
                packageName = project.property("package") as String
            }
            return packageName
        }

        /**
         * Get key name param from command line
         */
        @Input
        fun getKeyNameParam(): String {
            val chars = ('a'..'Z') + ('A'..'Z')
            var keyName = List(8) { chars.random() }.joinToString("")
            if (project.hasProperty("keyName")) {
                //From command line
                keyName = project.property("keyName") as String
            } else {
                println("Key name has been randomized, chose your own key name by adding argumen -PkeyName=yourName")
            }
            println("### KEY NAME ###\n$keyName\n")
            return keyName
        }

        /**
         * Generate en encoded key from command line params
         */
        fun getObfuscatedKey(): String {
            val key = getKeyParam()
            println("### SECRET ###\n$key\n")

            val packageName = getPackageNameParam()
            println("### PACKAGE NAME ###\n$packageName\n")

            val encodedKey = Utils.encodeSecret(key, packageName)
            println("### OBFUSCATED SECRET ###\n$encodedKey")
            return encodedKey
        }

        @OutputFile
        fun getCppDestination(fileName: String): File {
            return project.file(APP_MAIN_FOLDER + "cpp/$fileName")
        }

        @OutputFile
        fun getKotlinDestination(packageName: String, fileName: String): File {
            var path = APP_MAIN_FOLDER + "java/"
            packageName.split(".").forEach {
                path += "$it/"
            }
            val directory = project.file(path)
            if (!directory.exists()) {
                error("Directory $path does not exist in the project, you might have selected a wrong package.")
            }
            path += fileName
            return project.file(path)
        }

        /**
         * Unzip plugin into tmp directory
         */
        project.tasks.create(TASK_UNZIP_HIDDEN_SECRETS, Copy::class.java, object : Action<Copy?> {
            @TaskAction
            override fun execute(copy: Copy) {
                // in the case of buildSrc dir
                copy.from(project.zipTree(javaClass.protectionDomain.codeSource.location!!.toExternalForm()))
                println("Unzip jar to $tmpFolder")
                copy.into(tmpFolder)
            }
        })

        /**
         * Copy C++ files to your project
         */
        project.task(TASK_COPY_CPP) {
            doLast {
                project.file("$tmpFolder/cpp/").listFiles()?.forEach {
                    val destination = getCppDestination(it.name)
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        /**
         * Copy Kotlin file to your project
         */
        project.task(TASK_COPY_KOTLIN) {
            doLast {
                val packageName = getPackageNameParam()
                if (packageName.isEmpty()) {
                    error("Empty package name, use argument -Ppackage=your.package.name")
                }
                project.file("$tmpFolder/kotlin/").listFiles()?.forEach {
                    val destination = getKotlinDestination(packageName, it.name)
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        /**
         * Get an obfuscated key from command line
         */
        project.task(TASK_OBFUSCATE_KEY) {
            doLast {
                getObfuscatedKey()
            }
        }

        /**
         * Setup the project before to be able to hide secrets
         */
        project.task(TASK_SETUP_HIDDEN_SECRETS) {
            dependsOn(listOf(TASK_UNZIP_HIDDEN_SECRETS, TASK_COPY_CPP, TASK_COPY_KOTLIN))
            doLast{
                println("Hidden Secrets Plugin is ready !")
            }
        }

        /**
         * Obfuscate a key and add it to your Android project
         */
        project.task(TASK_HIDE_SECRET_KEY) {
            doLast {
                val keyName = getKeyNameParam()
                val packageName = getPackageNameParam()
                val obfuscatedKey = getObfuscatedKey()

                //Add obfuscated key in C++ code
                val secretsCpp = getCppDestination("secrets.cpp")
                if (secretsCpp.exists()) {
                    var text = secretsCpp.readText(Charset.defaultCharset())
                    if (text.contains(obfuscatedKey)) {
                        println("Key already added in C++ !")
                    }
                    if (text.contains("YOUR_KEY_GOES_HERE")) {
                        //Replace package name
                        text = text.replace("YOUR_PACKAGE_GOES_HERE", Utils.getUnderScoredPackageName(packageName))
                        //Replace key name
                        text = text.replace("YOUR_KEY_NAME_GOES_HERE", keyName)
                        //Replace demo key
                        text = text.replace("{YOUR_KEY_GOES_HERE}", obfuscatedKey)
                        secretsCpp.writeText(text)
                    } else {
                        //Add new key
                        text += CodeGenerator.getCppCode(packageName, keyName, obfuscatedKey)
                        secretsCpp.writeText(text)
                    }
                } else {
                    error("Missing C++ file, please run gradle task : $TASK_COPY_CPP")
                }

                //Add method in Kotlin code
                val secretsKotlin = getKotlinDestination(packageName, "Secrets.kt")
                if (secretsKotlin.exists()) {
                    var text = secretsKotlin.readText(Charset.defaultCharset())
                    text = text.replace("YOUR_PACKAGE_GOES_HERE", packageName)
                    if (text.contains(keyName)) {
                        println("Method already added in Kotlin !")
                    }
                    text = text.dropLast(1)
                    text += CodeGenerator.getKotlinCode(keyName)
                    secretsKotlin.writeText(text)
                } else {
                    error("Missing Kotlin file, please run gradle task : $TASK_COPY_KOTLIN")
                }

                println("You can now get your secret key by calling : Secrets().get$keyName(packageName)")
            }
        }
    }
}