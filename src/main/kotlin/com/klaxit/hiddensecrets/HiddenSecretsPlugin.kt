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
        private const val KEY_PLACEHOLDER = "YOUR_KEY_GOES_HERE"
        private const val PACKAGE_PLACEHOLDER = "YOUR_PACKAGE_GOES_HERE"
        private const val KOTLIN_FILE_NAME = "Secrets.kt"

        // Tasks
        const val TASK_UNZIP_HIDDEN_SECRETS = "unzipHiddenSecrets"
        const val TASK_COPY_CPP = "copyCpp"
        const val TASK_COPY_KOTLIN = "copyKotlin"
        const val TASK_HIDE_SECRET = "hideSecret"
        const val TASK_OBFUSCATE = "obfuscate"
        const val TASK_PACKAGE_NAME = "packageName"
        const val TASK_FIND_KOTLIN_FILE = "findKotlinFile"

        // Properties
        private const val KEY = "key"
        private const val KEY_NAME = "keyName"
        private const val PACKAGE = "package"

        // Errors
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
                // From command line
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
            var packageName: String? = null
            if (project.hasProperty(PACKAGE)) {
                // From command line
                packageName = project.property(PACKAGE) as String?
            }
            if (packageName.isNullOrEmpty()) {
                // From Android app
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
            val chars = ('a'..'z') + ('A'..'Z')
            //  Default random key name
            var keyName = List(DEFAULT_KEY_NAME_LENGTH) { chars.random() }.joinToString("")
            if (project.hasProperty(KEY_NAME)) {
                // From command line
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
                println("Directory $path does not exist in the project, you might have selected a wrong package.")
            }
            path += fileName
            return project.file(path)
        }

        /**
         * If found, returns the Secrets.kt file in the Android app
         */
        fun getKotlinFile(): File? {
            return Utils.findFileInProject(project, APP_MAIN_FOLDER, KOTLIN_FILE_NAME)
        }

        /**
         * Copy Cpp files from the lib to the Android project if they don't exist yet
         */
        fun copyCppFiles() {
            project.file("$tmpFolder/cpp/").listFiles()?.forEach {
                val destination = getCppDestination(it.name)
                if (destination.exists()) {
                    println("${it.name} already exists")
                } else {
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        /**
         * Copy Kotlin file Secrets.kt from the lib to the Android project if it does not exist yet
         */
        fun copyKotlinFile() {
            getKotlinFile()?.let {
                println("$KOTLIN_FILE_NAME already exists")
                return
            }
            val packageName = getPackageNameParam()
            project.file("$tmpFolder/kotlin/").listFiles()?.forEach {
                val destination = getKotlinDestination(packageName, it.name)
                if (destination.exists()) {
                    println("${it.name} already exists")
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
                    //  in the case of buildSrc dir
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
                copyKotlinFile()
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
                // Assert that the key is present
                getKeyParam()
                // Copy files if they don't exist
                copyCppFiles()
                copyKotlinFile()

                val keyName = getKeyNameParam()
                val packageName = getPackageNameParam()
                val obfuscatedKey = getObfuscatedKey()

                // Add method in Kotlin code
                var secretsKotlin = getKotlinFile()
                if (secretsKotlin == null) {
                    // File not found in project
                    secretsKotlin = getKotlinDestination(packageName, KOTLIN_FILE_NAME)
                }
                if (secretsKotlin.exists()) {
                    var text = secretsKotlin.readText(Charset.defaultCharset())
                    text = text.replace(PACKAGE_PLACEHOLDER, packageName)
                    if (text.contains(keyName)) {
                        println("⚠️ Method already added in Kotlin !")
                    }
                    text = text.dropLast(1)
                    text += CodeGenerator.getKotlinCode(keyName)
                    secretsKotlin.writeText(text)
                } else {
                    error("Missing Kotlin file, please run gradle task : $TASK_COPY_KOTLIN")
                }
                // Resolve package name for C++ from the one used in Kotlin file
                var kotlinPackage = Utils.getKotlinFilePackage(secretsKotlin)
                if (kotlinPackage.isEmpty()) {
                    println("Empty package in $KOTLIN_FILE_NAME")
                    kotlinPackage = packageName
                }

                // Add obfuscated key in C++ code
                val secretsCpp = getCppDestination("secrets.cpp")
                if (secretsCpp.exists()) {
                    var text = secretsCpp.readText(Charset.defaultCharset())
                    if (text.contains(obfuscatedKey)) {
                        println("⚠️ Key already added in C++ !")
                    }
                    if (text.contains(KEY_PLACEHOLDER)) {
                        // Edit placeholder key
                        // Replace package name
                        text = text.replace(PACKAGE_PLACEHOLDER, Utils.getCppPackageName(kotlinPackage))
                        // Replace key name
                        text = text.replace("YOUR_KEY_NAME_GOES_HERE", keyName)
                        // Replace demo key
                        text = text.replace(KEY_PLACEHOLDER, obfuscatedKey)
                        secretsCpp.writeText(text)
                    } else {
                        // Add new key
                        text += CodeGenerator.getCppCode(kotlinPackage, keyName, obfuscatedKey)
                        secretsCpp.writeText(text)
                    }
                } else {
                    error("Missing C++ file, please run gradle task : $TASK_COPY_CPP")
                }
                println("✅ You can now get your secret key by calling : Secrets().get$keyName(packageName)")
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

        /**
         * Find Secrets.kt file in the project
         */
        project.task(TASK_FIND_KOTLIN_FILE) {
            doLast {
                getKotlinFile()
            }
        }
    }
}
