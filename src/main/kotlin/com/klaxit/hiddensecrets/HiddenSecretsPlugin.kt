package com.klaxit.hiddensecrets

import com.android.build.gradle.AppExtension
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset

/**
 * Available gradle tasks from HiddenSecretsPlugin
 */
open class HiddenSecretsPlugin : Plugin<Project> {
    companion object {
        private const val APP_MAIN_FOLDER = "src/main/"
        private const val DEFAULT_KEY_NAME_LENGTH = 8

        //Tasks
        private const val TASK_UNZIP_HIDDEN_SECRETS = "unzipHiddenSecrets"
        private const val TASK_COPY_CPP = "copyCpp"
        private const val TASK_COPY_KOTLIN = "copyKotlin"
        private const val TASK_OBFUSCATE = "obfuscate"
        private const val TASK_HIDE_SECRET = "hideSecret"
        private const val TASK_PACKAGE_NAME = "packageName"

        //Properties
        private const val KEY = "key"
        private const val KEY_NAME = "keyName"
        private const val PACKAGE = "package"

        //Errors
        private const val ERROR_EMPTY_KEY = "No key provided, use argument '-Pkey=yourKey'"
        private const val ERROR_EMPTY_PACKAGE = "Empty package name, use argument '-Ppackage=your.package.name'"
    }

    override fun apply(project: Project) {

        val tmpFolder = java.lang.String.format("%s/hidden-secrets-tmp", project.buildDir)

        /**
         * Get the package name of the Android app on which this plugin is used
         */
        fun getAppPackageName(): String? {
            val androidExtension = project.extensions.getByName("android")

            if (androidExtension is AppExtension) {
                return androidExtension.defaultConfig.applicationId
            }
            return null
        }

        /**
         * Get key param from command line
         */
        @Input
        fun getKeyParam(): String {
            val key: String
            if (project.hasProperty(KEY)) {
                //From command line
                key = project.property(KEY) as String
            } else {
                throw InvalidUserDataException(ERROR_EMPTY_KEY)
            }
            return key
        }

        /**
         * Get package name param from command line
         */
        @Input
        fun getPackageNameParam(): String {
            //From config
            var packageName: String? = null
            if (project.hasProperty(PACKAGE)) {
                //From command line
                packageName = project.property(PACKAGE) as String?
            }
            if (packageName.isNullOrEmpty()) {
                //From Android app
                packageName = getAppPackageName()
            }
            if (packageName.isNullOrEmpty()) {
                throw InvalidUserDataException(ERROR_EMPTY_PACKAGE)
            }
            return packageName
        }

        /**
         * Get key name param from command line
         */
        @Input
        fun getKeyNameParam(): String {
            val chars = ('a'..'Z') + ('A'..'Z')
            // Default random key name
            var keyName = List(DEFAULT_KEY_NAME_LENGTH) { chars.random() }.joinToString("")
            if (project.hasProperty(KEY_NAME)) {
                //From command line
                keyName = project.property(KEY_NAME) as String
            } else {
                println("Key name has been randomized, chose your own key name by adding argument '-PkeyName=yourName'")
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
         * Copy Cpp files from the lib to the Android project if they don't exist yet
         */
        fun copyCppFiles() {
            project.file("$tmpFolder/cpp/").listFiles()?.forEach {
                val destination = getCppDestination(it.name)
                if (destination.exists()) {
                    println(it.name + " already exists")
                } else {
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        /**
         * Copy Kotlin files from the lib to the Android project if they don't exist yet
         */
        fun copyKotlinFiles() {
            val packageName = getPackageNameParam()
            project.file("$tmpFolder/kotlin/").listFiles()?.forEach {
                val destination = getKotlinDestination(packageName, it.name)
                if (destination.exists()) {
                    println(it.name + " already exists")
                } else {
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        /**
         * Unzip plugin into tmp directory
         */
        project.tasks.create(TASK_UNZIP_HIDDEN_SECRETS, Copy::class.java,
                object : Action<Copy?> {
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
        project.task(TASK_COPY_CPP)
        {
            doLast {
                copyCppFiles()
            }
        }

        /**
         * Copy Kotlin file to your project
         */
        project.task(TASK_COPY_KOTLIN)
        {
            doLast {
                copyKotlinFiles()
            }
        }

        /**
         * Get an obfuscated key from command line
         */
        project.task(TASK_OBFUSCATE)
        {
            doLast {
                getObfuscatedKey()
            }
        }

        /**
         * Obfuscate a key and add it to your Android project
         */
        project.task(TASK_HIDE_SECRET)
        {
            dependsOn(TASK_UNZIP_HIDDEN_SECRETS)

            doLast {
                //Copy files if they do not exist
                copyCppFiles()
                copyKotlinFiles()

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

        /**
         * Print the package name of the app
         */
        project.task(TASK_PACKAGE_NAME)
        {
            doLast {
                println("APP PACKAGE NAME = " + getPackageNameParam())
            }
        }
    }
}
