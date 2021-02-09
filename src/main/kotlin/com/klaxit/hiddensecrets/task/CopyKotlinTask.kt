package com.klaxit.hiddensecrets.task

import com.klaxit.hiddensecrets.Constants
import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import com.klaxit.hiddensecrets.Utils
import org.gradle.api.tasks.TaskAction

/**
 * Copy Kotlin file Secrets.kt from the lib to the Android project if it does not exist yet.
 */
open class CopyKotlinTask : BaseTask() {

    @TaskAction
    fun copyKotlinFile() {
        if (Utils.getKotlinFile(project) != null) {
            println("${Constants.KOTLIN_FILE_NAME} already exists")
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
}
