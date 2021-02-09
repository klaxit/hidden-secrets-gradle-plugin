package com.klaxit.hiddensecrets.task

import com.klaxit.hiddensecrets.CodeGenerator
import com.klaxit.hiddensecrets.Constants
import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import com.klaxit.hiddensecrets.Utils
import org.gradle.api.tasks.TaskAction
import java.nio.charset.Charset

/**
 * Main task of the plugin, will copy files, obfuscate the key and create get function.
 */
open class HideSecretTask : BaseTask() {
    companion object {
        private const val KEY_PLACEHOLDER = "YOUR_KEY_GOES_HERE"
        private const val PACKAGE_PLACEHOLDER = "YOUR_PACKAGE_GOES_HERE"
    }

    @TaskAction
    fun hideSecret() {
        //Assert that the key is present
        getKeyParam()
        //Copy files if they don't exist
        CopyCppTask().copyCppFiles()
        CopyKotlinTask().copyKotlinFile()

        val keyName = getKeyNameParam()
        val packageName = getPackageNameParam()
        val obfuscatedKey = getObfuscatedKey()

        //Add method in Kotlin code
        var secretsKotlin = Utils.getKotlinFile(project)
        if (secretsKotlin == null) {
            //File not found in project
            secretsKotlin = getKotlinDestination(packageName, Constants.KOTLIN_FILE_NAME)
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
            error("Missing Kotlin file, please run gradle task : ${HiddenSecretsPlugin.TASK_COPY_KOTLIN}")
        }
        //Resolve package name for C++ from the one used in Kotlin file
        var kotlinPackage = Utils.getKotlinFilePackage(secretsKotlin)
        if (kotlinPackage.isEmpty()) {
            println("Empty package in ${Constants.KOTLIN_FILE_NAME}")
            kotlinPackage = packageName
        }

        //Add obfuscated key in C++ code
        val secretsCpp = getCppDestination("secrets.cpp")
        if (secretsCpp.exists()) {
            var text = secretsCpp.readText(Charset.defaultCharset())
            if (text.contains(obfuscatedKey)) {
                println("⚠️ Key already added in C++ !")
            }
            if (text.contains(KEY_PLACEHOLDER)) {
                //Edit placeholder key
                //Replace package name
                text = text.replace(PACKAGE_PLACEHOLDER, Utils.getSnakeCasePackageName(kotlinPackage))
                //Replace key name
                text = text.replace("YOUR_KEY_NAME_GOES_HERE", keyName)
                //Replace demo key
                text = text.replace(KEY_PLACEHOLDER, obfuscatedKey)
                secretsCpp.writeText(text)
            } else {
                //Add new key
                text += CodeGenerator.getCppCode(kotlinPackage, keyName, obfuscatedKey)
                secretsCpp.writeText(text)
            }
        } else {
            error("Missing C++ file, please run gradle task : ${HiddenSecretsPlugin.TASK_COPY_CPP}")
        }
        println("✅ You can now get your secret key by calling : Secrets().get$keyName(packageName)")
    }
}
