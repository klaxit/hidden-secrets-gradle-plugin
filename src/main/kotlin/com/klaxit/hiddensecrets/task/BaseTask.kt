package com.klaxit.hiddensecrets.task

import com.klaxit.hiddensecrets.Constants
import com.klaxit.hiddensecrets.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.OutputFile
import java.io.File

/**
 * Mother class containing utilities to extend by Hidden Secrets tasks.
 */
abstract class BaseTask : DefaultTask() {
    companion object {
        private const val DEFAULT_KEY_NAME_LENGTH = 8
        // Params
        private const val KEY = "key"
        private const val KEY_NAME = "keyName"
        private const val PACKAGE = "package"
        // Errors
        private const val ERROR_EMPTY_KEY = "No key provided, use argument '-Pkey=yourKey'"
        private const val ERROR_EMPTY_PACKAGE = "Empty package name, use argument '-Ppackage=your.package.name'"
    }

    val tmpFolder: String = java.lang.String.format("%s/hidden-secrets-tmp", project.buildDir)

    /**
     * Get key param from command line
     */
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
     * Get key name param from command line
     */
    fun getKeyNameParam(): String {
        val chars = ('a'..'z') + ('A'..'Z')
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
     * Get package name param from command line
     */
    fun getPackageNameParam(): String {
        var packageName: String? = null
        if (project.hasProperty(PACKAGE)) {
            //From command line
            packageName = project.property(PACKAGE) as String?
        }
        if (packageName.isNullOrEmpty()) {
            //From Android app
            packageName = Utils.getAppPackageName(project)
        }
        if (packageName.isNullOrEmpty()) {
            throw InvalidUserDataException(ERROR_EMPTY_PACKAGE)
        }
        return packageName
    }

    /**
     * Returns the destination File for a cpp filename.
     */
    @OutputFile
    fun getCppDestination(fileName: String): File {
        return project.file(Constants.APP_MAIN_FOLDER + "cpp/$fileName")
    }

    /**
     * Returns the destination File for a kotlin filename.
     */
    @OutputFile
    fun getKotlinDestination(packageName: String, fileName: String): File {
        var path = Constants.APP_MAIN_FOLDER + "java/"
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
}
