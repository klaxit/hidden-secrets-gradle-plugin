package com.klaxit.hiddensecrets.task

import com.klaxit.hiddensecrets.HiddenSecretsPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import java.io.File

/**
 * Mother class containing utilities to extend by Hidden Secrets tasks.
 */
abstract class BaseTask : DefaultTask() {
    val tmpFolder = java.lang.String.format("%s/hidden-secrets-tmp", project.buildDir)

    @OutputFile
    fun getCppDestination(fileName: String): File {
        return project.file(HiddenSecretsPlugin.APP_MAIN_FOLDER + "cpp/$fileName")
    }
}