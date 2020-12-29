package com.klaxit.hiddensecrets.task

import org.gradle.api.tasks.TaskAction

/**
 * Print the Android app package. Useful to debug.
 */
open class PackageNameTask : BaseTask() {

    @TaskAction
    fun printAppPackageName() {
        println("APP PACKAGE NAME = " + getPackageNameParam())
    }
}
