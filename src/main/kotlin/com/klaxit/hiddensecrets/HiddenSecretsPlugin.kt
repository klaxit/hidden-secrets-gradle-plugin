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
import java.util.Properties

/**
 * Available gradle tasks from HiddenSecretsPlugin
 */
open class HiddenSecretsPlugin : Plugin<Project> {
    companion object {
        private const val APP_MAIN_FOLDER = "src/main/"
        private const val DEFAULT_KEY_NAME_LENGTH = 8
        private const val KEY_PLACEHOLDER = "YOUR_KEY_GOES_HERE"
        private const val PACKAGE_PLACEHOLDER = "YOUR_PACKAGE_GOES_HERE"
        private const val SECRET_KOTLIN_FILE_NAME = "Secrets.kt"
        private const val CERTIFICATE_FILE_NAME = "Certificate.kt"

        // Tasks
        const val TASK_GROUP = "Hide secrets"
        const val TASK_UNZIP_HIDDEN_SECRETS = "unzipHiddenSecrets"
        const val TASK_COPY_CPP = "copyCpp"
        const val TASK_COPY_KOTLIN = "copyKotlin"
        const val TASK_HIDE_SECRET = "hideSecret"
        const val TASK_HIDE_SECRET_FROM_PROPERTIES_FILE = "hideSecretFromPropertiesFile"
        const val TASK_OBFUSCATE = "obfuscate"
        const val TASK_PACKAGE_NAME = "packageName"
        const val TASK_CERT_KEY = "certKey"
        const val TASK_FIND_KOTLIN_FILE = "findKotlinFile"

        // Properties
        private const val PROP_KEY = "key"
        private const val PROP_KEY_NAME = "keyName"
        private const val PROP_PACKAGE = "package"
        private const val PROP_CERT_KEY = "certKey"
        private const val PROP_FILE_NAME = "propertiesFileName"

        // Errors
        private const val ERROR_EMPTY_KEY = "No key provided, use argument '-Pkey=yourKey'"
        private const val ERROR_EMPTY_PACKAGE = "Empty package name, use argument '-Ppackage=your.package.name'"
        private const val ERROR_EMPTY_CERT_KEY =
            "Empty cert key, use argument '-Pcert=150531447af80ddb41dce58a68c9ec3c'"

        // Sample usage
        private const val SAMPLE_FROM_PROPS = "-P${PROP_FILE_NAME}=credentials.properties"
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
            if (project.hasProperty(PROP_KEY)) {
                // From command line
                key = project.property(PROP_KEY) as String
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
            if (project.hasProperty(PROP_PACKAGE)) {
                // From command line
                packageName = project.property(PROP_PACKAGE) as String?
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
         * Get package name param from command line
         */
        @Input
        fun getCertMD5KeyParam(): String {
            var certificateMd5Key: String? = null
            if (project.hasProperty(PROP_CERT_KEY)) {
                // From command line
                certificateMd5Key = project.property(PROP_CERT_KEY) as String?
            }
            if (certificateMd5Key.isNullOrEmpty()) {
                // From Android app
                certificateMd5Key = getAppPackageName()
            }
            if (certificateMd5Key.isNullOrEmpty()) {
                throw InvalidUserDataException(ERROR_EMPTY_CERT_KEY)
            }
            return certificateMd5Key
        }

        /**
         * Get properties file path to hide secrets from
         */
        @Input
        fun getPropertiesFile(): File {
            return if (project.hasProperty(PROP_FILE_NAME)) {
                val propsPathRaw = project.property(PROP_FILE_NAME)
                if (propsPathRaw != null) {
                    File(project.rootDir, propsPathRaw as String)
                } else {
                    throw IllegalArgumentException(
                        "Cannot find properties (${propsPathRaw})!" +
                            " Use: '${SAMPLE_FROM_PROPS}'"
                    )
                }
            } else {
                throw IllegalArgumentException(
                    "Properties file is not defined!" +
                        " Use: '${SAMPLE_FROM_PROPS}'"
                )
            }
        }

        /**
         * Get properties from the provided file
         * @throws IllegalArgumentException no props found in project
         */
        @Throws(IllegalArgumentException::class)
        fun getPropertiesFromFile(propsFile: File): Properties {
            if (!propsFile.exists()) {
                throw IllegalArgumentException(
                    "Cannot find properties (${propsFile.absolutePath})!" +
                        " Use: '${SAMPLE_FROM_PROPS}'"
                )
            }
            return Properties().apply {
                propsFile.inputStream().use {
                    load(it) // Does not support UTF-8 characters
                }
            }
        }

        /**
         * Get key name param from command line
         */
        @Input
        fun getKeyNameParam(): String {
            val chars = ('a'..'z') + ('A'..'Z')
            // Default random key name
            var keyName = List(DEFAULT_KEY_NAME_LENGTH) { chars.random() }.joinToString("")
            if (project.hasProperty(PROP_KEY_NAME)) {
                // From command line
                keyName = project.property(PROP_KEY_NAME) as String
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

            val certKey = getCertMD5KeyParam()
            println("### Cert Key ###\n$certKey\n")

//            val packageName = getPackageNameParam()
//            println("### PACKAGE NAME ###\n$packageName\n")

            val encodedKey = Utils.encodeSecret(key, certKey)
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
         * @return empty template file for [fileName]
         */
        fun tmpKotlinFile(fileName: String): File {
            return project.file("$tmpFolder/kotlin/").listFiles()
                ?.first { it.name == fileName }
                ?: throw IllegalStateException("Did not find temporary template for secrets!")
        }

        /**
         * If found, returns the Secrets.kt file in the Android app
         */
        fun getKotlinFile(appMainFolder: String, kotlinFileName: String): File? {
            return Utils.findFileInProject(project, appMainFolder, kotlinFileName)
        }

        /**
         * Copy Cpp files from the lib to the Android project
         * @param overwrite whether to overwrite existing files
         */
        fun copyCppFiles(overwrite: Boolean = false) {
            project.file("$tmpFolder/cpp/").listFiles()?.forEach {
                val destination = getCppDestination(it.name)
                if (!overwrite && destination.exists()) {
                    println("${it.name} already exists")
                } else {
                    println("Copy $it.name to\n$destination")
                    it.copyTo(destination, true)
                }
            }
        }

        fun copyFile(existingKotlinFile: File?, overwrite: Boolean) {
            if (existingKotlinFile != null) {
                if (overwrite) {
                    println("Overwriting existing ${existingKotlinFile.name}.")
                    tmpKotlinFile(existingKotlinFile.name).copyTo(existingKotlinFile, true)
                } else {
                    println("${existingKotlinFile.name} already exists")
                    return
                }
            } else {
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
        }

        /**
         * Copy Kotlin file Secrets.kt from the lib to the Android project
         * @param overwrite whether to overwrite existing files
         */
        fun copyKotlinFile(overwrite: Boolean = false) {
            val existingKotlinFile: File? = getKotlinFile(APP_MAIN_FOLDER, SECRET_KOTLIN_FILE_NAME)
            val existingCertKotlinFile: File? = getKotlinFile(APP_MAIN_FOLDER, CERTIFICATE_FILE_NAME)
            copyFile(existingKotlinFile, overwrite)
            copyFile(existingCertKotlinFile, overwrite)

            val packageName = getPackageNameParam()
//            val existingCertKotlinFile = getKotlinDestination(packageName, CERTIFICATE_FILE_NAME)
            var text = existingCertKotlinFile?.readText(Charset.defaultCharset())
            text = text?.replace(PACKAGE_PLACEHOLDER, packageName)
            text = text?.dropLast(1)
            existingCertKotlinFile?.writeText(text!!)
        }

        /**
         * Main method of the project: add Cpp and Kotlin files to your project if necessary,
         * obfuscate your secret key and add it to your project.
         */
        fun hideSecret(
            keyName: String,
            packageName: String,
            obfuscatedKey: String
        ) {
            // Add method in Kotlin code
            var secretsKotlin = getKotlinFile(APP_MAIN_FOLDER, SECRET_KOTLIN_FILE_NAME)
            if (secretsKotlin == null) {
                // File not found in project
                secretsKotlin = getKotlinDestination(packageName, SECRET_KOTLIN_FILE_NAME)
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
                println("Empty package in $SECRET_KOTLIN_FILE_NAME")
                kotlinPackage = packageName
            }

            // Add obfuscated key in C++ code
            val secretsCpp = getCppDestination("secrets.cpp")
            if (secretsCpp.exists()) {
                var text = secretsCpp.readText(Charset.defaultCharset())
                if (text.contains(obfuscatedKey)) {
                    println("⚠️ Key already added in C++ !")
                }
                // Escape required characters
                val cppKeyName = Utils.getCppName(keyName)
                if (text.contains(KEY_PLACEHOLDER)) {
                    // Edit placeholder key
                    // Replace package name
                    text = text.replace(PACKAGE_PLACEHOLDER, Utils.getCppName(kotlinPackage))
                    // Replace key name
                    text = text.replace("YOUR_KEY_NAME_GOES_HERE", cppKeyName)
                    // Replace demo key
                    text = text.replace(KEY_PLACEHOLDER, obfuscatedKey)
                    secretsCpp.writeText(text)
                } else {
                    // Add new key
                    text += CodeGenerator.getCppCode(kotlinPackage, cppKeyName, obfuscatedKey)
                    secretsCpp.writeText(text)
                }
            } else {
                error("Missing C++ file, please run gradle task : $TASK_COPY_CPP")
            }
            println("✅ You can now get your secret key by calling : Secrets().get$keyName(packageName)")
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
            }).apply {
            this.group = TASK_GROUP
            this.description = "Unzip plugin into tmp directory"
        }

        /**
         * Copy C++ files to your project
         */
        project.task(TASK_COPY_CPP)
        {
            this.group = TASK_GROUP
            this.description = "Copy C++ files to your project"
            doLast {
                copyCppFiles()
            }
        }

        /**
         * Copy Kotlin file to your project
         */
        project.task(TASK_COPY_KOTLIN)
        {
            this.group = TASK_GROUP
            this.description = "Copy Kotlin file to your project"
            doLast {
                copyKotlinFile()
            }
        }

        /**
         * Get an obfuscated key from command line
         */
        project.task(TASK_OBFUSCATE)
        {
            this.group = TASK_GROUP
            this.description = "Get an obfuscated key from command line"
            doLast {
                getObfuscatedKey()
            }
        }

        /**
         * Obfuscate a key and add it to your Android project
         */
        project.task(TASK_HIDE_SECRET)
        {
            this.group = TASK_GROUP
            this.description = "Obfuscate a key and add it to your Android project"
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

                hideSecret(keyName, packageName, obfuscatedKey)
            }
        }

        /**
         * Clean all secret hidden keys in your project and obfuscate all keys from the properties file.
         */
        project.task(TASK_HIDE_SECRET_FROM_PROPERTIES_FILE)
        {
            this.group = TASK_GROUP
            this.description = "Re-generate and obfuscate keys from properties file and add it to your Android project"
            dependsOn(TASK_UNZIP_HIDDEN_SECRETS)

            doLast {
                // Create a clean copy of dependency files
                copyCppFiles(true)
                copyKotlinFile(true)

                val certKey = getCertMD5KeyParam()
                val packageName = getPackageNameParam()
                val propsFile = getPropertiesFile()
                val props = getPropertiesFromFile(propsFile = propsFile)
                println("Generating secrets from props: ${propsFile.path}")
                props.entries.forEach { entry ->
                    val keyName = entry.key as String
                    val obfuscatedKey = Utils.encodeSecret(entry.value as String, certKey)
                    hideSecret(keyName, packageName, obfuscatedKey)
                }
            }
        }

        /**
         * Print the package name of the app
         */
        project.task(TASK_PACKAGE_NAME)
        {
            this.group = TASK_GROUP
            this.description = "Print the package name of the app"
            doLast {
                println("APP PACKAGE NAME = " + getPackageNameParam())
            }
        }

        /**
         * Find Secrets.kt file in the project
         */
        project.task(TASK_FIND_KOTLIN_FILE) {
            this.group = TASK_GROUP
            this.description = "Find Secrets.kt file in the project"
            doLast {
                getKotlinFile(APP_MAIN_FOLDER, SECRET_KOTLIN_FILE_NAME)
            }
        }

        /**
         * Print the cert key of the app
         */
        project.task(TASK_CERT_KEY)
        {
            this.group = TASK_GROUP
            this.description = "Print the cert key of the app"
            doLast {
                println("APP CERT KEY = " + getCertMD5KeyParam())
            }
        }
    }
}
